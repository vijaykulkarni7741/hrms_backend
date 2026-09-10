package com.medimaze.HRMS.Incentive;

import com.medimaze.HRMS.Employee.dto.EmployeeDto;
import com.medimaze.HRMS.common.ApiResponse;
import com.medimaze.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncentiveService {

    private final IncentiveRepository incentiveRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Incentive> saveOrUpdateIncentive(Incentive incentive) {
        if (incentive == null) {
            return ApiResponse.error("Incentive payload cannot be null");
        }

        if (incentive.getId() != null && incentive.getId() > 0) {
            Incentive existing = incentiveRepository.findById(incentive.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Incentive not found with ID: " + incentive.getId());
            }

            if (incentive.getIncentiveName() != null) existing.setIncentiveName(incentive.getIncentiveName());
            if (incentive.getAmount() != null) existing.setAmount(incentive.getAmount());
            if (incentive.getIncentiveDate() != null) existing.setIncentiveDate(incentive.getIncentiveDate());
            if (incentive.getStatus() != null) existing.setStatus(incentive.getStatus());
            if (incentive.getIsActive() != null) existing.setIsActive(incentive.getIsActive());

            Incentive updated = incentiveRepository.save(existing);
            return ApiResponse.success("Incentive updated successfully", updated);
        }

        if (incentive.getIncentiveName() == null || incentive.getIncentiveName().trim().isEmpty()) {
            return ApiResponse.error("Incentive name is required");
        }

        if (incentive.getStatus() == null) incentive.setStatus("PENDING");
        if (incentive.getIsActive() == null) incentive.setIsActive(true);

        Incentive saved = incentiveRepository.save(incentive);
        return ApiResponse.success("Incentive created successfully", saved);
    }

    @Transactional
    public ApiResponse<Incentive> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Incentive ID is required");
        }
        Incentive incentive = incentiveRepository.findById(id).orElse(null);
        if (incentive == null) {
            return ApiResponse.error("Incentive not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(incentive.getIsActive());
        incentive.setIsActive(newStatus);
        incentive.setStatus(newStatus ? "PENDING" : "CANCELLED");

        Incentive updated = incentiveRepository.save(incentive);
        return ApiResponse.success("Incentive active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Incentive>> getIncentivesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<Incentive> list;
        if (unitId != null && unitId > 0) {
            list = incentiveRepository.findByUnitId(unitId);
        } else {
            list = incentiveRepository.findAll();
        }
        return ApiResponse.success("Incentives retrieved successfully", list);
    }
}
