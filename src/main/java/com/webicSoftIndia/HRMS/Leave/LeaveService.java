package com.webicSoftIndia.HRMS.Leave;

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
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Leave> saveOrUpdateLeave(Leave leave) {
        if (leave == null) {
            return ApiResponse.error("Leave payload cannot be null");
        }

        if (leave.getId() != null && leave.getId() > 0) {
            Leave existing = leaveRepository.findById(leave.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Leave not found with ID: " + leave.getId());
            }

            if (leave.getStartDate() != null) existing.setStartDate(leave.getStartDate());
            if (leave.getEndDate() != null) existing.setEndDate(leave.getEndDate());
            if (leave.getTotalDays() != null) existing.setTotalDays(leave.getTotalDays());
            if (leave.getReason() != null) existing.setReason(leave.getReason());
            if (leave.getStatus() != null) existing.setStatus(leave.getStatus());
            if (leave.getIsActive() != null) existing.setIsActive(leave.getIsActive());

            Leave updated = leaveRepository.save(existing);
            return ApiResponse.success("Leave updated successfully", updated);
        }

        if (leave.getStatus() == null) leave.setStatus("PENDING");
        if (leave.getIsActive() == null) leave.setIsActive(true);

        Leave saved = leaveRepository.save(leave);
        return ApiResponse.success("Leave created successfully", saved);
    }

    @Transactional
    public ApiResponse<Leave> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Leave ID is required");
        }
        Leave leave = leaveRepository.findById(id).orElse(null);
        if (leave == null) {
            return ApiResponse.error("Leave not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(leave.getIsActive());
        leave.setIsActive(newStatus);
        leave.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Leave updated = leaveRepository.save(leave);
        return ApiResponse.success("Leave active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Leave>> getLeavesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<Leave> list;
        if (unitId != null && unitId > 0) {
            list = leaveRepository.findByUnitId(unitId);
        } else {
            list = leaveRepository.findAll();
        }
        return ApiResponse.success("Leaves retrieved successfully", list);
    }
}
