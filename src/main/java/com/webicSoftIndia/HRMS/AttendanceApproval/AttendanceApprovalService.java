package com.webicSoftIndia.HRMS.AttendanceApproval;

import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceApprovalService {

    private final AttendanceApprovalRepository approvalRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<AttendanceApproval> saveOrUpdateApproval(AttendanceApproval approval) {
        if (approval == null) {
            return ApiResponse.error("AttendanceApproval payload cannot be null");
        }

        if (approval.getId() != null && approval.getId() > 0) {
            AttendanceApproval existing = approvalRepository.findById(approval.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("AttendanceApproval not found with ID: " + approval.getId());
            }

            if (approval.getStatus() != null) {
                existing.setStatus(approval.getStatus());
            }
            if (approval.getComments() != null) {
                existing.setComments(approval.getComments());
            }
            if (approval.getActionDate() != null) {
                existing.setActionDate(approval.getActionDate());
            }

            AttendanceApproval updated = approvalRepository.save(existing);
            return ApiResponse.success("AttendanceApproval updated successfully", updated);
        }

        if (approval.getReferenceType() == null || approval.getReferenceType().trim().isEmpty()) {
            return ApiResponse.error("Reference type is required");
        }
        if (approval.getReferenceId() == null) {
            return ApiResponse.error("Reference ID is required");
        }
        if (approval.getStatus() == null) {
            approval.setStatus("PENDING");
        }

        AttendanceApproval saved = approvalRepository.save(approval);
        return ApiResponse.success("AttendanceApproval created successfully", saved);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<AttendanceApproval>> getApprovalsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<AttendanceApproval> list = approvalRepository.findAll();
        return ApiResponse.success("Attendance approvals retrieved successfully", list);
    }
}
