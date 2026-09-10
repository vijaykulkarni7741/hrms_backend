package com.medimaze.HRMS.City;

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
public class CityService {

    private final CityRepository cityRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<City> saveOrUpdateCity(City city) {
        if (city == null) {
            return ApiResponse.error("City payload cannot be null");
        }

        // UPDATE MODE
        if (city.getId() != null && city.getId() > 0) {
            City existing = cityRepository.findById(city.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("City not found with ID: " + city.getId());
            }

            if (city.getCityName() != null && !city.getCityName().trim().isEmpty()) {
                existing.setCityName(city.getCityName().trim());
            }
            if (city.getState() != null) {
                existing.setState(city.getState());
            }
            if (city.getStatus() != null) {
                existing.setStatus(city.getStatus());
            }
            if (city.getIsActive() != null) {
                existing.setIsActive(city.getIsActive());
            }

            City updated = cityRepository.save(existing);
            log.info("Updated City ID: {}", updated.getId());
            return ApiResponse.success("City updated successfully", updated);
        }

        // CREATE MODE
        if (city.getCityName() == null || city.getCityName().trim().isEmpty()) {
            return ApiResponse.error("City name is required");
        }

        if (city.getStatus() == null) {
            city.setStatus("ACTIVE");
        }
        if (city.getIsActive() == null) {
            city.setIsActive(true);
        }

        City saved = cityRepository.save(city);
        log.info("Created City ID: {}", saved.getId());
        return ApiResponse.success("City created successfully", saved);
    }

    @Transactional
    public ApiResponse<City> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid City ID is required");
        }
        City city = cityRepository.findById(id).orElse(null);
        if (city == null) {
            return ApiResponse.error("City not found with ID: " + id);
        }

        boolean currentStatus = Boolean.TRUE.equals(city.getIsActive());
        boolean newStatus = !currentStatus;
        city.setIsActive(newStatus);
        city.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        City updated = cityRepository.save(city);
        log.info("Toggled active status for City ID: {} to {}", id, newStatus);
        return ApiResponse.success("City active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<City>> getCitiesBySessionUnit(String sessionId) {
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
            if (sessionData != null && sessionData.getUnitId() != null && sessionData.getUnitId() > 0) {
                // Could filter by unit in the future
            }
        }
        List<City> list = cityRepository.findAll();
        return ApiResponse.success("Cities retrieved successfully", list);
    }
}

