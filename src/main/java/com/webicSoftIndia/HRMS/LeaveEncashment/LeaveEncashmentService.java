package com.webicSoftIndia.HRMS.LeaveEncashment;

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
public class LeaveEncashmentService {

    private final LeaveEncashmentRepository leaveEncashmentRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<LeaveEncashment> saveOrUpdateLeaveEncashment(LeaveEncashment encashment) {
        if (encashment == null) {
            return ApiResponse.error("LeaveEncashment payload cannot be null");
        }

        if (encashment.getId() != null && encashment.getId() > 0) {
            LeaveEncashment existing = leaveEncashmentRepository.findById(encashment.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("LeaveEncashment not found with ID: " + encashment.getId());
            }

            if (encashment.getEncashDays() != null) existing.setEncashDays(encashment.getEncashDays());
            if (encashment.getEncashAmount() != null) existing.setEncashAmount(encashment.getEncashAmount());
            if (encashment.getStatus() != null) existing.setStatus(encashment.getStatus());
            if (encashment.getIsActive() != null) existing.setIsActive(encashment.getIsActive());

            LeaveEncashment updated = leaveEncashmentRepository.save(existing);
            return ApiResponse.success("LeaveEncashment updated successfully", updated);
        }

        if (encashment.getStatus() == null) encashment.setStatus("PENDING");
        if (encashment.getIsActive() == null) encashment.setIsActive(true);

        LeaveEncashment saved = leaveEncashmentRepository.save(encashment);
        return ApiResponse.success("LeaveEncashment created successfully", saved);
    }

    @Transactional
    public ApiResponse<LeaveEncashment> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid LeaveEncashment ID is required");
        }
        LeaveEncashment encashment = leaveEncashmentRepository.findById(id).orElse(null);
        if (encashment == null) {
            return ApiResponse.error("LeaveEncashment not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(encashment.getIsActive());
        encashment.setIsActive(newStatus);
        encashment.setStatus(newStatus ? "PENDING" : "CANCELLED");

        LeaveEncashment updated = leaveEncashmentRepository.save(encashment);
        return ApiResponse.success("LeaveEncashment active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<LeaveEncashment>> getLeaveEncashmentsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<LeaveEncashment> list;
        if (unitId != null && unitId > 0) {
            list = leaveEncashmentRepository.findByUnitId(unitId);
        } else {
            list = leaveEncashmentRepository.findAll();
        }
        return ApiResponse.success("LeaveEncashments retrieved successfully", list);
    }
}
