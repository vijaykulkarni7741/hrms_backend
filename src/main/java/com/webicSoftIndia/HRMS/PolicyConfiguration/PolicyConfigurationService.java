package com.webicSoftIndia.HRMS.PolicyConfiguration;

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
public class PolicyConfigurationService {

    private final PolicyConfigurationRepository policyConfigurationRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<PolicyConfiguration> saveOrUpdatePolicyConfiguration(PolicyConfiguration config) {
        if (config == null) {
            return ApiResponse.error("PolicyConfiguration payload cannot be null");
        }

        if (config.getId() != null && config.getId() > 0) {
            PolicyConfiguration existing = policyConfigurationRepository.findById(config.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("PolicyConfiguration not found with ID: " + config.getId());
            }

            if (config.getConfigKey() != null) existing.setConfigKey(config.getConfigKey());
            if (config.getConfigValue() != null) existing.setConfigValue(config.getConfigValue());
            if (config.getStatus() != null) existing.setStatus(config.getStatus());
            if (config.getIsActive() != null) existing.setIsActive(config.getIsActive());

            PolicyConfiguration updated = policyConfigurationRepository.save(existing);
            return ApiResponse.success("PolicyConfiguration updated successfully", updated);
        }

        if (config.getConfigKey() == null || config.getConfigKey().trim().isEmpty()) {
            return ApiResponse.error("Config key is required");
        }

        if (config.getStatus() == null) config.setStatus("ACTIVE");
        if (config.getIsActive() == null) config.setIsActive(true);

        PolicyConfiguration saved = policyConfigurationRepository.save(config);
        return ApiResponse.success("PolicyConfiguration created successfully", saved);
    }

    @Transactional
    public ApiResponse<PolicyConfiguration> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid PolicyConfiguration ID is required");
        }
        PolicyConfiguration config = policyConfigurationRepository.findById(id).orElse(null);
        if (config == null) {
            return ApiResponse.error("PolicyConfiguration not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(config.getIsActive());
        config.setIsActive(newStatus);
        config.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        PolicyConfiguration updated = policyConfigurationRepository.save(config);
        return ApiResponse.success("PolicyConfiguration active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<PolicyConfiguration>> getPolicyConfigurationsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<PolicyConfiguration> list;
        if (unitId != null && unitId > 0) {
            list = policyConfigurationRepository.findByUnitId(unitId);
        } else {
            list = policyConfigurationRepository.findAll();
        }
        return ApiResponse.success("PolicyConfigurations retrieved successfully", list);
    }
}
