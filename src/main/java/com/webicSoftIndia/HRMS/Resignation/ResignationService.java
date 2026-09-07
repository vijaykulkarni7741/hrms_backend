package com.webicSoftIndia.HRMS.Resignation;

import com.webicSoftIndia.HRMS.Employee.Employee;
import com.webicSoftIndia.HRMS.Employee.EmployeeRepository;
import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.Organization.Organization;
import com.webicSoftIndia.HRMS.Organization.OrganizationRepository;
import com.webicSoftIndia.HRMS.Unit.Unit;
import com.webicSoftIndia.HRMS.Unit.UnitRepository;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.common.EmailNotificationService;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResignationService {

    private final ResignationRepository resignationRepository;
    private final RedisSessionService redisSessionService;
    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final UnitRepository unitRepository;
    private final EmailNotificationService emailNotificationService;

    @Transactional
    public ApiResponse<Resignation> submitResignationFromMap(Map<String, Object> payload, String sessionId) {
        if (payload == null) {
            return ApiResponse.error("Resignation payload cannot be null");
        }

        Long employeeId = payload.get("employeeId") != null ? Long.valueOf(payload.get("employeeId").toString()) : null;
        String reason = payload.get("reason") != null ? payload.get("reason").toString() : "Career Growth / Personal Reasons";
        Integer noticePeriodDays = payload.get("noticePeriodDays") != null ? Integer.valueOf(payload.get("noticePeriodDays").toString()) : 30;

        LocalDate resignationDate = LocalDate.now();
        if (payload.get("resignationDate") != null && !payload.get("resignationDate").toString().trim().isEmpty()) {
            try {
                resignationDate = LocalDate.parse(payload.get("resignationDate").toString().substring(0, 10));
            } catch (Exception ignored) {}
        }

        LocalDate lastWorkingDay = resignationDate.plusDays(noticePeriodDays);
        if (payload.get("lastWorkingDay") != null && !payload.get("lastWorkingDay").toString().trim().isEmpty()) {
            try {
                lastWorkingDay = LocalDate.parse(payload.get("lastWorkingDay").toString().substring(0, 10));
            } catch (Exception ignored) {}
        }

        Employee employee = null;
        if (employeeId != null && employeeId > 0) {
            employee = employeeRepository.findById(employeeId).orElse(null);
        }
        if (employee == null && sessionId != null && !sessionId.trim().isEmpty()) {
            EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
            if (sessionData != null && sessionData.getId() != null) {
                employee = employeeRepository.findById(sessionData.getId()).orElse(null);
            }
        }
        if (employee == null) {
            employee = employeeRepository.findAll().stream().findFirst().orElse(null);
        }
        if (employee == null) {
            return ApiResponse.error("Valid employee profile required to submit resignation.");
        }

        // WORK MANAGER BYPASS RULE:
        // Check if employee has a Work Manager assigned
        Long workingManagerId = employee.getWorkingManagerId();
        boolean hasWorkManager = workingManagerId != null && workingManagerId > 0;

        Resignation resignation = Resignation.builder()
                .employee(employee)
                .organization(employee.getOrganization() != null ? employee.getOrganization() : organizationRepository.findAll().stream().findFirst().orElse(null))
                .unit(employee.getUnit() != null ? employee.getUnit() : unitRepository.findAll().stream().findFirst().orElse(null))
                .resignationDate(resignationDate)
                .noticePeriodDays(noticePeriodDays)
                .lastWorkingDay(lastWorkingDay)
                .reason(reason)
                .status("PENDING")
                .requiresWorkManagerApproval(hasWorkManager)
                .reportingManagerStatus("PENDING")
                .workManagerStatus(hasWorkManager ? "PENDING" : "BYPASSED")
                .hrStatus("PENDING")
                .isActive(true)
                .build();

        Resignation saved = resignationRepository.save(resignation);

        // Fetch Manager Email details for Email & Notification dispatch
        String rmEmail = null;
        if (employee.getReportingManagerId() != null && employee.getReportingManagerId() > 0) {
            Employee rm = employeeRepository.findById(employee.getReportingManagerId()).orElse(null);
            if (rm != null) rmEmail = rm.getOfficialEmail() != null ? rm.getOfficialEmail() : rm.getEmail();
        }
        if (rmEmail == null) rmEmail = "reporting.manager@webicsoft.in";

        String wmEmail = null;
        if (hasWorkManager) {
            Employee wm = employeeRepository.findById(workingManagerId).orElse(null);
            if (wm != null) wmEmail = wm.getOfficialEmail() != null ? wm.getOfficialEmail() : wm.getEmail();
        }

        // Dispatch Email & Notification
        emailNotificationService.sendResignationNotification(
                employee,
                resignationDate.toString(),
                lastWorkingDay.toString(),
                reason,
                rmEmail,
                wmEmail
        );

        return ApiResponse.success("Resignation request submitted successfully! Notifications sent to managers & HR.", saved);
    }

    @Transactional
    public ApiResponse<Resignation> approveOrRejectResignation(Map<String, Object> payload) {
        if (payload == null || payload.get("id") == null) {
            return ApiResponse.error("Resignation ID is required");
        }

        Long id = Long.valueOf(payload.get("id").toString());
        String action = payload.get("action") != null ? payload.get("action").toString().toUpperCase() : "APPROVE";
        String role = payload.get("role") != null ? payload.get("role").toString().toUpperCase() : "HR";
        String comments = payload.get("comments") != null ? payload.get("comments").toString() : "";

        Resignation resignation = resignationRepository.findById(id).orElse(null);
        if (resignation == null) {
            return ApiResponse.error("Resignation record not found with ID: " + id);
        }

        String targetStatus = "REJECT".equals(action) || "REJECTED".equals(action) ? "REJECTED" : "APPROVED";

        if ("WORK_MANAGER".equals(role) || "WM".equals(role)) {
            resignation.setWorkManagerStatus(targetStatus);
        } else if ("REPORTING_MANAGER".equals(role) || "RM".equals(role)) {
            resignation.setReportingManagerStatus(targetStatus);
        } else {
            resignation.setHrStatus(targetStatus);
        }

        if (comments != null && !comments.trim().isEmpty()) {
            resignation.setComments(comments.trim());
        }

        // Evaluate overall status
        boolean isRmApproved = "APPROVED".equalsIgnoreCase(resignation.getReportingManagerStatus());
        boolean isWmApproved = !"PENDING".equalsIgnoreCase(resignation.getWorkManagerStatus()) && !"REJECTED".equalsIgnoreCase(resignation.getWorkManagerStatus());
        boolean isHrApproved = "APPROVED".equalsIgnoreCase(resignation.getHrStatus());

        if ("REJECTED".equalsIgnoreCase(targetStatus)) {
            resignation.setStatus("REJECTED");
        } else if (isRmApproved && isWmApproved && isHrApproved) {
            resignation.setStatus("APPROVED");
        } else if (isRmApproved || isHrApproved) {
            resignation.setStatus("PARTIALLY_APPROVED");
        }

        Resignation updated = resignationRepository.save(resignation);
        return ApiResponse.success("Resignation status updated to " + updated.getStatus(), updated);
    }

    @Transactional
    public ApiResponse<Resignation> saveOrUpdateResignation(Resignation resignation) {
        if (resignation == null) {
            return ApiResponse.error("Resignation payload cannot be null");
        }

        if (resignation.getId() != null && resignation.getId() > 0) {
            Resignation existing = resignationRepository.findById(resignation.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Resignation record not found with ID: " + resignation.getId());
            }

            if (resignation.getResignationDate() != null) existing.setResignationDate(resignation.getResignationDate());
            if (resignation.getNoticePeriodDays() != null) existing.setNoticePeriodDays(resignation.getNoticePeriodDays());
            if (resignation.getLastWorkingDay() != null) existing.setLastWorkingDay(resignation.getLastWorkingDay());
            if (resignation.getReason() != null) existing.setReason(resignation.getReason());
            if (resignation.getStatus() != null) existing.setStatus(resignation.getStatus());
            if (resignation.getIsActive() != null) existing.setIsActive(resignation.getIsActive());

            Resignation updated = resignationRepository.save(existing);
            return ApiResponse.success("Resignation updated successfully", updated);
        }

        if (resignation.getResignationDate() == null) {
            resignation.setResignationDate(LocalDate.now());
        }

        if (resignation.getStatus() == null) resignation.setStatus("PENDING");
        if (resignation.getIsActive() == null) resignation.setIsActive(true);

        Resignation saved = resignationRepository.save(resignation);
        return ApiResponse.success("Resignation created successfully", saved);
    }

    @Transactional
    public ApiResponse<Resignation> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Resignation ID is required");
        }
        Resignation resignation = resignationRepository.findById(id).orElse(null);
        if (resignation == null) {
            return ApiResponse.error("Resignation record not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(resignation.getIsActive());
        resignation.setIsActive(newStatus);
        resignation.setStatus(newStatus ? "PENDING" : "INACTIVE");

        Resignation updated = resignationRepository.save(resignation);
        return ApiResponse.success("Resignation active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Resignation>> getResignationsBySessionUnit(String sessionId) {
        List<Resignation> list = resignationRepository.findAll();
        return ApiResponse.success("Resignations retrieved successfully", list);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Resignation>> getResignationsByEmployee(Long employeeId, String sessionId) {
        if (employeeId != null && employeeId > 0) {
            List<Resignation> list = resignationRepository.findByEmployeeId(employeeId);
            return ApiResponse.success("Resignations retrieved successfully", list);
        }
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
            if (sessionData != null && sessionData.getId() != null) {
                List<Resignation> list = resignationRepository.findByEmployeeId(sessionData.getId());
                if (!list.isEmpty()) {
                    return ApiResponse.success("Resignations retrieved for session employee", list);
                }
            }
        }
        List<Resignation> list = resignationRepository.findAll();
        return ApiResponse.success("Resignations retrieved successfully", list);
    }
}
