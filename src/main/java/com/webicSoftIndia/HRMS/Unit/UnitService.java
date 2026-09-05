package com.webicSoftIndia.HRMS.Unit;

import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnitService {

    private final UnitRepository unitRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Unit> saveOrUpdateUnit(Unit unit) {
        if (unit == null) {
            return ApiResponse.error("Unit payload cannot be null");
        }

        // UPDATE MODE
        if (unit.getId() != null && unit.getId() > 0) {
            Unit existing = unitRepository.findById(unit.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Unit not found with ID: " + unit.getId());
            }

            if (unit.getUnitName() != null && !unit.getUnitName().trim().isEmpty()) {
                existing.setUnitName(unit.getUnitName().trim());
            }
            if (unit.getUnitType() != null) {
                existing.setUnitType(unit.getUnitType());
            }
            if (unit.getHospitalCode() != null) {
                existing.setHospitalCode(unit.getHospitalCode());
            }
            if (unit.getLicenseNumber() != null) {
                existing.setLicenseNumber(unit.getLicenseNumber());
            }
            if (unit.getEmail() != null) {
                existing.setEmail(unit.getEmail());
            }
            if (unit.getMobileNumber() != null) {
                existing.setMobileNumber(unit.getMobileNumber());
            }
            if (unit.getLandlineNumber() != null) {
                existing.setLandlineNumber(unit.getLandlineNumber());
            }
            if (unit.getAddressLine1() != null) {
                existing.setAddressLine1(unit.getAddressLine1());
            }
            if (unit.getAddressLine2() != null) {
                existing.setAddressLine2(unit.getAddressLine2());
            }
            if (unit.getPinCode() != null) {
                existing.setPinCode(unit.getPinCode());
            }
            if (unit.getLatitude() != null) {
                existing.setLatitude(unit.getLatitude());
            }
            if (unit.getLongitude() != null) {
                existing.setLongitude(unit.getLongitude());
            }
            if (unit.getAllowedRadius() != null) {
                existing.setAllowedRadius(unit.getAllowedRadius());
            }
            if (unit.getTimeZone() != null) {
                existing.setTimeZone(unit.getTimeZone());
            }
            if (unit.getRemarks() != null) {
                existing.setRemarks(unit.getRemarks());
            }
            if (unit.getStatus() != null) {
                existing.setStatus(unit.getStatus());
            }
            if (unit.getIsActive() != null) {
                existing.setIsActive(unit.getIsActive());
            }

            Unit updated = unitRepository.save(existing);
            log.info("Updated Unit ID: {}", updated.getId());
            return ApiResponse.success("Unit updated successfully", updated);
        }

        // CREATE MODE
        if (unit.getUnitCode() == null || unit.getUnitCode().trim().isEmpty()) {
            return ApiResponse.error("Unit code is required");
        }
        if (unit.getUnitName() == null || unit.getUnitName().trim().isEmpty()) {
            return ApiResponse.error("Unit name is required");
        }

        if (unitRepository.findByUnitCode(unit.getUnitCode().trim()).isPresent()) {
            return ApiResponse.error("Unit code already exists");
        }

        if (unit.getStatus() == null) {
            unit.setStatus("ACTIVE");
        }
        if (unit.getIsActive() == null) {
            unit.setIsActive(true);
        }

        Unit saved = unitRepository.save(unit);
        log.info("Created Unit ID: {}", saved.getId());
        return ApiResponse.success("Unit created successfully", saved);
    }

    @Transactional
    public ApiResponse<Unit> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Unit ID is required");
        }
        Unit unit = unitRepository.findById(id).orElse(null);
        if (unit == null) {
            return ApiResponse.error("Unit not found with ID: " + id);
        }

        boolean currentStatus = Boolean.TRUE.equals(unit.getIsActive());
        boolean newStatus = !currentStatus;
        unit.setIsActive(newStatus);
        unit.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Unit updated = unitRepository.save(unit);
        log.info("Toggled active status for Unit ID: {} to {}", id, newStatus);
        return ApiResponse.success("Unit active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Unit>> getUnitsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long sessionUnitId = sessionData.getUnitId();
        List<Unit> list;
        if (sessionUnitId != null && sessionUnitId > 0) {
            list = unitRepository.findById(sessionUnitId)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        } else {
            list = unitRepository.findAll();
        }
        return ApiResponse.success("Units retrieved successfully", list);
    }
}

