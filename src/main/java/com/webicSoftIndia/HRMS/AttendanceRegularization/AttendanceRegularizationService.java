package com.webicSoftIndia.HRMS.AttendanceRegularization;

import com.webicSoftIndia.HRMS.Attendance.Attendance;
import com.webicSoftIndia.HRMS.Attendance.AttendanceRepository;
import com.webicSoftIndia.HRMS.AttendanceApproval.AttendanceApproval;
import com.webicSoftIndia.HRMS.AttendanceApproval.AttendanceApprovalRepository;
import com.webicSoftIndia.HRMS.AttendanceRegularization.dto.RegularizationApprovalRequest;
import com.webicSoftIndia.HRMS.Employee.Employee;
import com.webicSoftIndia.HRMS.Employee.EmployeeRepository;
import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceRegularizationService {

    private final AttendanceRegularizationRepository regularizationRepository;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceApprovalRepository approvalRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<AttendanceRegularization> saveOrUpdateRegularization(AttendanceRegularization regularization) {
        if (regularization == null) {
            return ApiResponse.error("AttendanceRegularization payload cannot be null");
        }

        if (regularization.getId() != null && regularization.getId() > 0) {
            AttendanceRegularization existing = regularizationRepository.findById(regularization.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("AttendanceRegularization not found with ID: " + regularization.getId());
            }

            if (regularization.getStatus() != null) {
                existing.setStatus(regularization.getStatus());
            }
            if (regularization.getApprovalRemarks() != null) {
                existing.setApprovalRemarks(regularization.getApprovalRemarks());
            }

            AttendanceRegularization updated = regularizationRepository.save(existing);
            return ApiResponse.success("AttendanceRegularization updated successfully", updated);
        }

        if (regularization.getRegularizationDate() == null) {
            return ApiResponse.error("Regularization date is required");
        }
        if (regularization.getStatus() == null) {
            regularization.setStatus("PENDING");
        }

        AttendanceRegularization saved = regularizationRepository.save(regularization);
        return ApiResponse.success("AttendanceRegularization created successfully", saved);
    }

    @Transactional
    public ApiResponse<AttendanceRegularization> processRegularizationApproval(RegularizationApprovalRequest request, String sessionId) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            return ApiResponse.error("Regularization request ID is required");
        }

        String targetStatus = request.getStatus();
        if (targetStatus == null || (!"APPROVED".equalsIgnoreCase(targetStatus.trim()) && !"REJECTED".equalsIgnoreCase(targetStatus.trim()))) {
            return ApiResponse.error("Invalid status. Approval status must be APPROVED or REJECTED");
        }
        targetStatus = targetStatus.trim().toUpperCase();

        AttendanceRegularization regularization = regularizationRepository.findById(request.getId()).orElse(null);
        if (regularization == null) {
            return ApiResponse.error("AttendanceRegularization record not found with ID: " + request.getId());
        }

        Employee approver = null;
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
            if (sessionData != null && sessionData.getId() != null) {
                approver = employeeRepository.findById(sessionData.getId()).orElse(null);
            }
        }
        if (approver == null && request.getApproverId() != null && request.getApproverId() > 0) {
            approver = employeeRepository.findById(request.getApproverId()).orElse(null);
        }

        regularization.setStatus(targetStatus);
        if (approver != null) {
            regularization.setApprovedBy(approver);
        }
        if (request.getRemarks() != null) {
            regularization.setApprovalRemarks(request.getRemarks().trim());
        }
        AttendanceRegularization savedReg = regularizationRepository.save(regularization);

        // Update AttendanceApproval record
        Optional<AttendanceApproval> approvalOpt = approvalRepository.findByReferenceTypeAndReferenceId("REGULARIZATION", savedReg.getId());
        AttendanceApproval approvalRecord;
        if (approvalOpt.isPresent()) {
            approvalRecord = approvalOpt.get();
        } else {
            approvalRecord = AttendanceApproval.builder()
                    .referenceType("REGULARIZATION")
                    .referenceId(savedReg.getId())
                    .approvalLevel(1)
                    .build();
        }
        approvalRecord.setStatus(targetStatus);
        if (approver != null) {
            approvalRecord.setApprover(approver);
        }
        if (request.getRemarks() != null) {
            approvalRecord.setComments(request.getRemarks().trim());
        }
        approvalRecord.setActionDate(LocalDateTime.now());
        approvalRepository.save(approvalRecord);

        // Update Attendance record: PRESENT if APPROVED, ABSENT if REJECTED
        Attendance attendance = savedReg.getAttendance();
        if (attendance == null && savedReg.getEmployee() != null && savedReg.getRegularizationDate() != null) {
            Optional<Attendance> existingAttOpt = attendanceRepository.findTopByEmployeeIdAndAttendanceDateOrderByCreatedDateDesc(
                    savedReg.getEmployee().getId(), savedReg.getRegularizationDate());
            attendance = existingAttOpt.orElseGet(() -> Attendance.builder()
                    .employee(savedReg.getEmployee())
                    .unit(savedReg.getEmployee().getUnit())
                    .attendanceDate(savedReg.getRegularizationDate())
                    .isActive(true)
                    .build());
        }

        if (attendance != null) {
            if ("APPROVED".equals(targetStatus)) {
                attendance.setStatus("PRESENT");

                LocalTime inTime = savedReg.getRequestedInTime() != null ? savedReg.getRequestedInTime() : LocalTime.of(9, 30);
                LocalTime outTime = savedReg.getRequestedOutTime() != null ? savedReg.getRequestedOutTime() : LocalTime.of(18, 30);

                attendance.setInTime(inTime);
                attendance.setOutTime(outTime);

                long minutes = Duration.between(inTime, outTime).toMinutes();
                double hours = Math.max(0, minutes / 60.0);
                attendance.setTotalHours(BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP));

                String note = "Regularization Approved";
                if (request.getRemarks() != null && !request.getRemarks().trim().isEmpty()) {
                    note += ": " + request.getRemarks().trim();
                }
                attendance.setRemarks(note);
            } else if ("REJECTED".equals(targetStatus)) {
                attendance.setStatus("ABSENT");
                String note = "Regularization Rejected";
                if (request.getRemarks() != null && !request.getRemarks().trim().isEmpty()) {
                    note += ": " + request.getRemarks().trim();
                }
                attendance.setRemarks(note);
            }
            attendanceRepository.save(attendance);
        }

        log.info("AttendanceRegularization ID {} status updated to {} by approver ID {}", savedReg.getId(), targetStatus, approver != null ? approver.getId() : "N/A");
        return ApiResponse.success("Attendance regularization request " + targetStatus.toLowerCase() + " successfully", savedReg);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<AttendanceRegularization>> getRegularizationsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<AttendanceRegularization> list = regularizationRepository.findAll();
        return ApiResponse.success("Attendance regularizations retrieved successfully", list);
    }
}
