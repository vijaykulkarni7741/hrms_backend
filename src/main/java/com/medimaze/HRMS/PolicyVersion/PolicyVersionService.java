package com.medimaze.HRMS.PolicyVersion;

import com.medimaze.HRMS.Employee.dto.EmployeeDto;
import com.medimaze.HRMS.PolicyHistory.PolicyHistory;
import com.medimaze.HRMS.PolicyHistory.PolicyHistoryRepository;
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
public class PolicyVersionService {

    private final PolicyVersionRepository policyVersionRepository;
    private final PolicyHistoryRepository policyHistoryRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<PolicyVersion> saveOrUpdatePolicyVersion(PolicyVersion version) {
        if (version == null) {
            return ApiResponse.error("PolicyVersion payload cannot be null");
        }

        if (version.getId() != null && version.getId() > 0) {
            PolicyVersion existing = policyVersionRepository.findById(version.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("PolicyVersion not found with ID: " + version.getId());
            }

            if (version.getVersionNumber() != null) existing.setVersionNumber(version.getVersionNumber());
            if (version.getDocumentUrl() != null) existing.setDocumentUrl(version.getDocumentUrl());
            if (version.getEffectiveFrom() != null) existing.setEffectiveFrom(version.getEffectiveFrom());
            if (version.getStatus() != null) existing.setStatus(version.getStatus());
            if (version.getIsActive() != null) existing.setIsActive(version.getIsActive());

            PolicyVersion updated = policyVersionRepository.save(existing);
            logHistory(updated, "UPDATE_VERSION", "Policy version updated to " + updated.getVersionNumber());
            return ApiResponse.success("PolicyVersion updated successfully", updated);
        }

        if (version.getVersionNumber() == null || version.getVersionNumber().trim().isEmpty()) {
            return ApiResponse.error("Version number is required");
        }

        if (version.getStatus() == null) version.setStatus("ACTIVE");
        if (version.getIsActive() == null) version.setIsActive(true);

        PolicyVersion saved = policyVersionRepository.save(version);
        logHistory(saved, "NEW_VERSION", "New policy version created: " + saved.getVersionNumber());
        return ApiResponse.success("PolicyVersion created successfully", saved);
    }

    @Transactional
    public ApiResponse<PolicyVersion> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid PolicyVersion ID is required");
        }
        PolicyVersion version = policyVersionRepository.findById(id).orElse(null);
        if (version == null) {
            return ApiResponse.error("PolicyVersion not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(version.getIsActive());
        version.setIsActive(newStatus);
        version.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        PolicyVersion updated = policyVersionRepository.save(version);
        logHistory(updated, "TOGGLE_VERSION_ACTIVE", "Policy version " + updated.getVersionNumber() + " active status toggled to " + newStatus);
        return ApiResponse.success("PolicyVersion active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<PolicyVersion>> getPolicyVersionsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<PolicyVersion> list;
        if (unitId != null && unitId > 0) {
            list = policyVersionRepository.findByUnitId(unitId);
        } else {
            list = policyVersionRepository.findAll();
        }
        return ApiResponse.success("PolicyVersions retrieved successfully", list);
    }

    private void logHistory(PolicyVersion version, String action, String changeDetails) {
        try {
            if (version.getPolicy() != null) {
                PolicyHistory history = PolicyHistory.builder()
                        .organization(version.getOrganization())
                        .unit(version.getUnit())
                        .policy(version.getPolicy())
                        .action(action)
                        .changeDetails(changeDetails)
                        .status("ACTIVE")
                        .isActive(true)
                        .build();
                policyHistoryRepository.save(history);
            }
        } catch (Exception e) {
            log.error("Failed to auto-save PolicyHistory for PolicyVersion ID {}: {}", version.getId(), e.getMessage());
        }
    }
}
