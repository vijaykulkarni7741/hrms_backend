package com.medimaze.HRMS.LeavePolicy;

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
public class LeavePolicyService {

    private final LeavePolicyRepository leavePolicyRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<LeavePolicy> saveOrUpdateLeavePolicy(LeavePolicy policy) {
        if (policy == null) {
            return ApiResponse.error("LeavePolicy payload cannot be null");
        }

        if (policy.getId() != null && policy.getId() > 0) {
            LeavePolicy existing = leavePolicyRepository.findById(policy.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("LeavePolicy not found with ID: " + policy.getId());
            }

            if (policy.getPolicyName() != null) existing.setPolicyName(policy.getPolicyName());
            if (policy.getMaxLeavesPerYear() != null) existing.setMaxLeavesPerYear(policy.getMaxLeavesPerYear());
            if (policy.getCarryForwardLimit() != null) existing.setCarryForwardLimit(policy.getCarryForwardLimit());
            if (policy.getDescription() != null) existing.setDescription(policy.getDescription());
            if (policy.getStatus() != null) existing.setStatus(policy.getStatus());
            if (policy.getIsActive() != null) existing.setIsActive(policy.getIsActive());

            LeavePolicy updated = leavePolicyRepository.save(existing);
            return ApiResponse.success("LeavePolicy updated successfully", updated);
        }

        if (policy.getPolicyName() == null || policy.getPolicyName().trim().isEmpty()) {
            return ApiResponse.error("Policy name is required");
        }

        if (policy.getStatus() == null) policy.setStatus("ACTIVE");
        if (policy.getIsActive() == null) policy.setIsActive(true);

        LeavePolicy saved = leavePolicyRepository.save(policy);
        return ApiResponse.success("LeavePolicy created successfully", saved);
    }

    @Transactional
    public ApiResponse<LeavePolicy> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid LeavePolicy ID is required");
        }
        LeavePolicy policy = leavePolicyRepository.findById(id).orElse(null);
        if (policy == null) {
            return ApiResponse.error("LeavePolicy not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(policy.getIsActive());
        policy.setIsActive(newStatus);
        policy.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        LeavePolicy updated = leavePolicyRepository.save(policy);
        return ApiResponse.success("LeavePolicy active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<LeavePolicy>> getLeavePoliciesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<LeavePolicy> list;
        if (unitId != null && unitId > 0) {
            list = leavePolicyRepository.findByUnitId(unitId);
        } else {
            list = leavePolicyRepository.findAll();
        }
        return ApiResponse.success("LeavePolicies retrieved successfully", list);
    }
}
