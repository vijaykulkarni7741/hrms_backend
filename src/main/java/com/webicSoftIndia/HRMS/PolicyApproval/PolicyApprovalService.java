package com.webicSoftIndia.HRMS.PolicyApproval;

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
public class PolicyApprovalService {

    private final PolicyApprovalRepository policyApprovalRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<PolicyApproval> saveOrUpdatePolicyApproval(PolicyApproval approval) {
        if (approval == null) {
            return ApiResponse.error("PolicyApproval payload cannot be null");
        }

        if (approval.getId() != null && approval.getId() > 0) {
            PolicyApproval existing = policyApprovalRepository.findById(approval.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("PolicyApproval not found with ID: " + approval.getId());
            }

            if (approval.getAction() != null) existing.setAction(approval.getAction());
            if (approval.getComments() != null) existing.setComments(approval.getComments());
            if (approval.getStatus() != null) existing.setStatus(approval.getStatus());
            if (approval.getIsActive() != null) existing.setIsActive(approval.getIsActive());

            PolicyApproval updated = policyApprovalRepository.save(existing);
            return ApiResponse.success("PolicyApproval updated successfully", updated);
        }

        if (approval.getStatus() == null) approval.setStatus("PENDING");
        if (approval.getIsActive() == null) approval.setIsActive(true);

        PolicyApproval saved = policyApprovalRepository.save(approval);
        return ApiResponse.success("PolicyApproval created successfully", saved);
    }

    @Transactional
    public ApiResponse<PolicyApproval> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid PolicyApproval ID is required");
        }
        PolicyApproval approval = policyApprovalRepository.findById(id).orElse(null);
        if (approval == null) {
            return ApiResponse.error("PolicyApproval not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(approval.getIsActive());
        approval.setIsActive(newStatus);
        approval.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        PolicyApproval updated = policyApprovalRepository.save(approval);
        return ApiResponse.success("PolicyApproval active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<PolicyApproval>> getPolicyApprovalsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<PolicyApproval> list;
        if (unitId != null && unitId > 0) {
            list = policyApprovalRepository.findByUnitId(unitId);
        } else {
            list = policyApprovalRepository.findAll();
        }
        return ApiResponse.success("PolicyApprovals retrieved successfully", list);
    }
}
