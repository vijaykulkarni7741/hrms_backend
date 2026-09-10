package com.medimaze.HRMS.Country;

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
public class CountryService {

    private final CountryRepository countryRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Country> saveOrUpdateCountry(Country country) {
        if (country == null) {
            return ApiResponse.error("Country payload cannot be null");
        }

        // UPDATE MODE
        if (country.getId() != null && country.getId() > 0) {
            Country existing = countryRepository.findById(country.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Country not found with ID: " + country.getId());
            }

            if (country.getCountryName() != null && !country.getCountryName().trim().isEmpty()) {
                existing.setCountryName(country.getCountryName().trim());
            }
            if (country.getPhoneCode() != null) {
                existing.setPhoneCode(country.getPhoneCode());
            }
            if (country.getStatus() != null) {
                existing.setStatus(country.getStatus());
            }
            if (country.getIsActive() != null) {
                existing.setIsActive(country.getIsActive());
            }

            Country updated = countryRepository.save(existing);
            log.info("Updated Country ID: {}", updated.getId());
            return ApiResponse.success("Country updated successfully", updated);
        }

        // CREATE MODE
        if (country.getCountryCode() == null || country.getCountryCode().trim().isEmpty()) {
            return ApiResponse.error("Country code is required");
        }
        if (country.getCountryName() == null || country.getCountryName().trim().isEmpty()) {
            return ApiResponse.error("Country name is required");
        }

        if (countryRepository.findByCountryCode(country.getCountryCode().trim()).isPresent()) {
            return ApiResponse.error("Country code already exists");
        }

        if (country.getStatus() == null) {
            country.setStatus("ACTIVE");
        }
        if (country.getIsActive() == null) {
            country.setIsActive(true);
        }

        Country saved = countryRepository.save(country);
        log.info("Created Country ID: {}", saved.getId());
        return ApiResponse.success("Country created successfully", saved);
    }

    @Transactional
    public ApiResponse<Country> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Country ID is required");
        }
        Country country = countryRepository.findById(id).orElse(null);
        if (country == null) {
            return ApiResponse.error("Country not found with ID: " + id);
        }

        boolean currentStatus = Boolean.TRUE.equals(country.getIsActive());
        boolean newStatus = !currentStatus;
        country.setIsActive(newStatus);
        country.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Country updated = countryRepository.save(country);
        log.info("Toggled active status for Country ID: {} to {}", id, newStatus);
        return ApiResponse.success("Country active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Country>> getCountriesBySessionUnit(String sessionId) {
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
            if (sessionData != null && sessionData.getUnitId() != null && sessionData.getUnitId() > 0) {
                // Return all countries for now as countries are global
            }
        }
        List<Country> list = countryRepository.findAll();
        return ApiResponse.success("Countries retrieved successfully", list);
    }
}

