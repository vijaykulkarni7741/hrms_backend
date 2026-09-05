package com.webicSoftIndia.HRMS.PolicyHistory;

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
public class PolicyHistoryService {

    private final PolicyHistoryRepository policyHistoryRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<PolicyHistory> saveOrUpdatePolicyHistory(PolicyHistory history) {
        if (history == null) {
            return ApiResponse.error("PolicyHistory payload cannot be null");
        }

        if (history.getId() != null && history.getId() > 0) {
            PolicyHistory existing = policyHistoryRepository.findById(history.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("PolicyHistory not found with ID: " + history.getId());
            }

            if (history.getAction() != null) existing.setAction(history.getAction());
            if (history.getChangeDetails() != null) existing.setChangeDetails(history.getChangeDetails());
            if (history.getStatus() != null) existing.setStatus(history.getStatus());
            if (history.getIsActive() != null) existing.setIsActive(history.getIsActive());

            PolicyHistory updated = policyHistoryRepository.save(existing);
            return ApiResponse.success("PolicyHistory updated successfully", updated);
        }

        if (history.getStatus() == null) history.setStatus("ACTIVE");
        if (history.getIsActive() == null) history.setIsActive(true);

        PolicyHistory saved = policyHistoryRepository.save(history);
        return ApiResponse.success("PolicyHistory created successfully", saved);
    }

    @Transactional
    public ApiResponse<PolicyHistory> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid PolicyHistory ID is required");
        }
        PolicyHistory history = policyHistoryRepository.findById(id).orElse(null);
        if (history == null) {
            return ApiResponse.error("PolicyHistory not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(history.getIsActive());
        history.setIsActive(newStatus);
        history.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        PolicyHistory updated = policyHistoryRepository.save(history);
        return ApiResponse.success("PolicyHistory active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<PolicyHistory>> getPolicyHistoriesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<PolicyHistory> list;
        if (unitId != null && unitId > 0) {
            list = policyHistoryRepository.findByUnitId(unitId);
        } else {
            list = policyHistoryRepository.findAll();
        }
        return ApiResponse.success("PolicyHistories retrieved successfully", list);
    }
}
