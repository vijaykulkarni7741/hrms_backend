package com.webicSoftIndia.HRMS.Policy;

import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.PolicyHistory.PolicyHistory;
import com.webicSoftIndia.HRMS.PolicyHistory.PolicyHistoryRepository;
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
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyHistoryRepository policyHistoryRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Policy> saveOrUpdatePolicy(Policy policy) {
        if (policy == null) {
            return ApiResponse.error("Policy payload cannot be null");
        }

        if (policy.getId() != null && policy.getId() > 0) {
            Policy existing = policyRepository.findById(policy.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Policy not found with ID: " + policy.getId());
            }

            StringBuilder details = new StringBuilder("Updated fields: ");
            if (policy.getPolicyTitle() != null) {
                details.append("title (").append(existing.getPolicyTitle()).append(" -> ").append(policy.getPolicyTitle()).append("); ");
                existing.setPolicyTitle(policy.getPolicyTitle());
            }
            if (policy.getCategory() != null) {
                details.append("category (").append(existing.getCategory()).append(" -> ").append(policy.getCategory()).append("); ");
                existing.setCategory(policy.getCategory());
            }
            if (policy.getDescription() != null) {
                details.append("description updated; ");
                existing.setDescription(policy.getDescription());
            }
            if (policy.getStatus() != null) {
                details.append("status (").append(existing.getStatus()).append(" -> ").append(policy.getStatus()).append("); ");
                existing.setStatus(policy.getStatus());
            }
            if (policy.getIsActive() != null) {
                details.append("isActive (").append(existing.getIsActive()).append(" -> ").append(policy.getIsActive()).append("); ");
                existing.setIsActive(policy.getIsActive());
            }

            Policy updated = policyRepository.save(existing);
            logHistory(updated, "UPDATE", details.toString());
            return ApiResponse.success("Policy updated successfully", updated);
        }

        if (policy.getPolicyTitle() == null || policy.getPolicyTitle().trim().isEmpty()) {
            return ApiResponse.error("Policy title is required");
        }

        if (policy.getStatus() == null) policy.setStatus("ACTIVE");
        if (policy.getIsActive() == null) policy.setIsActive(true);

        Policy saved = policyRepository.save(policy);
        logHistory(saved, "CREATE", "Policy created: " + saved.getPolicyTitle());
        return ApiResponse.success("Policy created successfully", saved);
    }

    @Transactional
    public ApiResponse<Policy> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Policy ID is required");
        }
        Policy policy = policyRepository.findById(id).orElse(null);
        if (policy == null) {
            return ApiResponse.error("Policy not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(policy.getIsActive());
        policy.setIsActive(newStatus);
        policy.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Policy updated = policyRepository.save(policy);
        logHistory(updated, "TOGGLE_ACTIVE", "Policy active status toggled to " + newStatus);
        return ApiResponse.success("Policy active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Policy>> getPoliciesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<Policy> list;
        if (unitId != null && unitId > 0) {
            list = policyRepository.findByUnitId(unitId);
        } else {
            list = policyRepository.findAll();
        }
        return ApiResponse.success("Policies retrieved successfully", list);
    }

    @Transactional
    public ApiResponse<Policy> submitForApproval(Long id, String username) {
        if (id == null || id <= 0) return ApiResponse.error("Valid Policy ID is required");
        Policy policy = policyRepository.findById(id).orElse(null);
        if (policy == null) return ApiResponse.error("Policy not found with ID: " + id);

        policy.setStatus("PENDING_APPROVAL");
        policy.setUpdatedBy(username);
        Policy saved = policyRepository.save(policy);
        logHistory(saved, "SUBMIT_APPROVAL", "Submitted for HR Manager approval by " + username);
        return ApiResponse.success("Policy submitted for approval successfully", saved);
    }

    @Transactional
    public ApiResponse<Policy> approvePolicy(Long id, String approvedBy) {
        if (id == null || id <= 0) return ApiResponse.error("Valid Policy ID is required");
        Policy policy = policyRepository.findById(id).orElse(null);
        if (policy == null) return ApiResponse.error("Policy not found with ID: " + id);

        policy.setStatus("APPROVED");
        policy.setIsActive(true);
        policy.setApprovedBy(approvedBy);
        policy.setUpdatedBy(approvedBy);
        Policy saved = policyRepository.save(policy);
        logHistory(saved, "APPROVE", "Approved by HR Manager: " + approvedBy);
        return ApiResponse.success("Policy approved successfully", saved);
    }

    @Transactional
    public ApiResponse<Policy> rejectPolicy(Long id, String rejectedBy, String reason) {
        if (id == null || id <= 0) return ApiResponse.error("Valid Policy ID is required");
        Policy policy = policyRepository.findById(id).orElse(null);
        if (policy == null) return ApiResponse.error("Policy not found with ID: " + id);

        policy.setStatus("REJECTED");
        policy.setIsActive(false);
        policy.setRejectionReason(reason);
        policy.setUpdatedBy(rejectedBy);
        Policy saved = policyRepository.save(policy);
        logHistory(saved, "REJECT", "Rejected by HR Manager (" + rejectedBy + "). Reason: " + reason);
        return ApiResponse.success("Policy rejected successfully", saved);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Policy>> getAllPolicies() {
        List<Policy> list = policyRepository.findAll();
        return ApiResponse.success("Policies retrieved successfully", list);
    }

    private void logHistory(Policy policy, String action, String changeDetails) {
        try {
            PolicyHistory history = PolicyHistory.builder()
                    .organization(policy.getOrganization())
                    .unit(policy.getUnit())
                    .policy(policy)
                    .action(action)
                    .changeDetails(changeDetails)
                    .status("ACTIVE")
                    .isActive(true)
                    .build();
            policyHistoryRepository.save(history);
        } catch (Exception e) {
            log.error("Failed to auto-save PolicyHistory for policy ID {}: {}", policy.getId(), e.getMessage());
        }
    }
}
