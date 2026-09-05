package com.webicSoftIndia.HRMS.ESICConfiguration;

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
public class ESICConfigurationService {

    private final ESICConfigurationRepository esicConfigurationRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<ESICConfiguration> saveOrUpdateESICConfiguration(ESICConfiguration config) {
        if (config == null) {
            return ApiResponse.error("ESICConfiguration payload cannot be null");
        }

        if (config.getId() != null && config.getId() > 0) {
            ESICConfiguration existing = esicConfigurationRepository.findById(config.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("ESICConfiguration not found with ID: " + config.getId());
            }

            if (config.getEmployeeContributionRate() != null) existing.setEmployeeContributionRate(config.getEmployeeContributionRate());
            if (config.getEmployerContributionRate() != null) existing.setEmployerContributionRate(config.getEmployerContributionRate());
            if (config.getWageLimit() != null) existing.setWageLimit(config.getWageLimit());
            if (config.getStatus() != null) existing.setStatus(config.getStatus());
            if (config.getIsActive() != null) existing.setIsActive(config.getIsActive());

            ESICConfiguration updated = esicConfigurationRepository.save(existing);
            return ApiResponse.success("ESICConfiguration updated successfully", updated);
        }

        if (config.getStatus() == null) config.setStatus("ACTIVE");
        if (config.getIsActive() == null) config.setIsActive(true);

        ESICConfiguration saved = esicConfigurationRepository.save(config);
        return ApiResponse.success("ESICConfiguration created successfully", saved);
    }

    @Transactional
    public ApiResponse<ESICConfiguration> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid ESICConfiguration ID is required");
        }
        ESICConfiguration config = esicConfigurationRepository.findById(id).orElse(null);
        if (config == null) {
            return ApiResponse.error("ESICConfiguration not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(config.getIsActive());
        config.setIsActive(newStatus);
        config.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        ESICConfiguration updated = esicConfigurationRepository.save(config);
        return ApiResponse.success("ESICConfiguration active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ESICConfiguration>> getESICConfigurationsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<ESICConfiguration> list;
        if (unitId != null && unitId > 0) {
            list = esicConfigurationRepository.findByUnitId(unitId);
        } else {
            list = esicConfigurationRepository.findAll();
        }
        return ApiResponse.success("ESICConfigurations retrieved successfully", list);
    }
}
