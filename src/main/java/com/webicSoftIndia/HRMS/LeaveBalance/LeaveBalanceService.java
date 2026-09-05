package com.webicSoftIndia.HRMS.LeaveBalance;

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
public class LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<LeaveBalance> saveOrUpdateLeaveBalance(LeaveBalance leaveBalance) {
        if (leaveBalance == null) {
            return ApiResponse.error("LeaveBalance payload cannot be null");
        }

        if (leaveBalance.getId() != null && leaveBalance.getId() > 0) {
            LeaveBalance existing = leaveBalanceRepository.findById(leaveBalance.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("LeaveBalance not found with ID: " + leaveBalance.getId());
            }

            if (leaveBalance.getYear() != null) existing.setYear(leaveBalance.getYear());
            if (leaveBalance.getAllocatedDays() != null) existing.setAllocatedDays(leaveBalance.getAllocatedDays());
            if (leaveBalance.getUsedDays() != null) existing.setUsedDays(leaveBalance.getUsedDays());
            if (leaveBalance.getRemainingDays() != null) existing.setRemainingDays(leaveBalance.getRemainingDays());
            if (leaveBalance.getStatus() != null) existing.setStatus(leaveBalance.getStatus());
            if (leaveBalance.getIsActive() != null) existing.setIsActive(leaveBalance.getIsActive());

            LeaveBalance updated = leaveBalanceRepository.save(existing);
            return ApiResponse.success("LeaveBalance updated successfully", updated);
        }

        if (leaveBalance.getStatus() == null) leaveBalance.setStatus("ACTIVE");
        if (leaveBalance.getIsActive() == null) leaveBalance.setIsActive(true);

        LeaveBalance saved = leaveBalanceRepository.save(leaveBalance);
        return ApiResponse.success("LeaveBalance created successfully", saved);
    }

    @Transactional
    public ApiResponse<LeaveBalance> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid LeaveBalance ID is required");
        }
        LeaveBalance leaveBalance = leaveBalanceRepository.findById(id).orElse(null);
        if (leaveBalance == null) {
            return ApiResponse.error("LeaveBalance not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(leaveBalance.getIsActive());
        leaveBalance.setIsActive(newStatus);
        leaveBalance.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        LeaveBalance updated = leaveBalanceRepository.save(leaveBalance);
        return ApiResponse.success("LeaveBalance active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<LeaveBalance>> getLeaveBalancesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<LeaveBalance> list;
        if (unitId != null && unitId > 0) {
            list = leaveBalanceRepository.findByUnitId(unitId);
        } else {
            list = leaveBalanceRepository.findAll();
        }
        return ApiResponse.success("LeaveBalances retrieved successfully", list);
    }
}
