package com.webicSoftIndia.HRMS.LeaveApplication;

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
public class LeaveApplicationService {

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<LeaveApplication> saveOrUpdateLeaveApplication(LeaveApplication application) {
        if (application == null) {
            return ApiResponse.error("LeaveApplication payload cannot be null");
        }

        if (application.getId() != null && application.getId() > 0) {
            LeaveApplication existing = leaveApplicationRepository.findById(application.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("LeaveApplication not found with ID: " + application.getId());
            }

            if (application.getStartDate() != null) existing.setStartDate(application.getStartDate());
            if (application.getEndDate() != null) existing.setEndDate(application.getEndDate());
            if (application.getTotalDays() != null) existing.setTotalDays(application.getTotalDays());
            if (application.getReason() != null) existing.setReason(application.getReason());
            if (application.getStatus() != null) existing.setStatus(application.getStatus());
            if (application.getIsActive() != null) existing.setIsActive(application.getIsActive());

            LeaveApplication updated = leaveApplicationRepository.save(existing);
            return ApiResponse.success("LeaveApplication updated successfully", updated);
        }

        if (application.getStatus() == null) application.setStatus("PENDING");
        if (application.getIsActive() == null) application.setIsActive(true);

        LeaveApplication saved = leaveApplicationRepository.save(application);
        return ApiResponse.success("LeaveApplication created successfully", saved);
    }

    @Transactional
    public ApiResponse<LeaveApplication> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid LeaveApplication ID is required");
        }
        LeaveApplication application = leaveApplicationRepository.findById(id).orElse(null);
        if (application == null) {
            return ApiResponse.error("LeaveApplication not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(application.getIsActive());
        application.setIsActive(newStatus);
        application.setStatus(newStatus ? "PENDING" : "CANCELLED");

        LeaveApplication updated = leaveApplicationRepository.save(application);
        return ApiResponse.success("LeaveApplication active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<LeaveApplication>> getLeaveApplicationsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<LeaveApplication> list;
        if (unitId != null && unitId > 0) {
            list = leaveApplicationRepository.findByUnitId(unitId);
        } else {
            list = leaveApplicationRepository.findAll();
        }
        return ApiResponse.success("LeaveApplications retrieved successfully", list);
    }
}
