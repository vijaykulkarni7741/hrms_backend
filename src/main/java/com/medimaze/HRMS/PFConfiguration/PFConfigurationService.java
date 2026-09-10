package com.medimaze.HRMS.PFConfiguration;

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
public class PFConfigurationService {

    private final PFConfigurationRepository pfConfigurationRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<PFConfiguration> saveOrUpdatePFConfiguration(PFConfiguration config) {
        if (config == null) {
            return ApiResponse.error("PFConfiguration payload cannot be null");
        }

        if (config.getId() != null && config.getId() > 0) {
            PFConfiguration existing = pfConfigurationRepository.findById(config.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("PFConfiguration not found with ID: " + config.getId());
            }

            if (config.getEmployeeContributionRate() != null) existing.setEmployeeContributionRate(config.getEmployeeContributionRate());
            if (config.getEmployerContributionRate() != null) existing.setEmployerContributionRate(config.getEmployerContributionRate());
            if (config.getWageCeiling() != null) existing.setWageCeiling(config.getWageCeiling());
            if (config.getStatus() != null) existing.setStatus(config.getStatus());
            if (config.getIsActive() != null) existing.setIsActive(config.getIsActive());

            PFConfiguration updated = pfConfigurationRepository.save(existing);
            return ApiResponse.success("PFConfiguration updated successfully", updated);
        }

        if (config.getStatus() == null) config.setStatus("ACTIVE");
        if (config.getIsActive() == null) config.setIsActive(true);

        PFConfiguration saved = pfConfigurationRepository.save(config);
        return ApiResponse.success("PFConfiguration created successfully", saved);
    }

    @Transactional
    public ApiResponse<PFConfiguration> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid PFConfiguration ID is required");
        }
        PFConfiguration config = pfConfigurationRepository.findById(id).orElse(null);
        if (config == null) {
            return ApiResponse.error("PFConfiguration not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(config.getIsActive());
        config.setIsActive(newStatus);
        config.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        PFConfiguration updated = pfConfigurationRepository.save(config);
        return ApiResponse.success("PFConfiguration active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<PFConfiguration>> getPFConfigurationsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<PFConfiguration> list;
        if (unitId != null && unitId > 0) {
            list = pfConfigurationRepository.findByUnitId(unitId);
        } else {
            list = pfConfigurationRepository.findAll();
        }
        return ApiResponse.success("PFConfigurations retrieved successfully", list);
    }
}
