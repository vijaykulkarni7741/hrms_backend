package com.webicSoftIndia.HRMS.Geofence;

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
public class GeofenceService {

    private final GeofenceRepository geofenceRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Geofence> saveOrUpdateGeofence(Geofence geofence) {
        if (geofence == null) {
            return ApiResponse.error("Geofence payload cannot be null");
        }

        if (geofence.getId() != null && geofence.getId() > 0) {
            Geofence existing = geofenceRepository.findById(geofence.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Geofence not found with ID: " + geofence.getId());
            }

            if (geofence.getFenceName() != null) {
                existing.setFenceName(geofence.getFenceName());
            }
            if (geofence.getLatitude() != null) {
                existing.setLatitude(geofence.getLatitude());
            }
            if (geofence.getLongitude() != null) {
                existing.setLongitude(geofence.getLongitude());
            }
            if (geofence.getRadiusMeters() != null) {
                existing.setRadiusMeters(geofence.getRadiusMeters());
            }
            if (geofence.getStatus() != null) {
                existing.setStatus(geofence.getStatus());
            }
            if (geofence.getIsActive() != null) {
                existing.setIsActive(geofence.getIsActive());
            }

            Geofence updated = geofenceRepository.save(existing);
            return ApiResponse.success("Geofence updated successfully", updated);
        }

        if (geofence.getFenceName() == null || geofence.getFenceName().trim().isEmpty()) {
            return ApiResponse.error("Fence name is required");
        }

        if (geofence.getStatus() == null) {
            geofence.setStatus("ACTIVE");
        }
        if (geofence.getIsActive() == null) {
            geofence.setIsActive(true);
        }

        Geofence saved = geofenceRepository.save(geofence);
        return ApiResponse.success("Geofence created successfully", saved);
    }

    @Transactional
    public ApiResponse<Geofence> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Geofence ID is required");
        }
        Geofence geofence = geofenceRepository.findById(id).orElse(null);
        if (geofence == null) {
            return ApiResponse.error("Geofence not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(geofence.getIsActive());
        geofence.setIsActive(newStatus);
        geofence.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Geofence updated = geofenceRepository.save(geofence);
        return ApiResponse.success("Geofence active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Geofence>> getGeofencesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<Geofence> list;
        if (unitId != null && unitId > 0) {
            list = geofenceRepository.findByUnitId(unitId);
        } else {
            list = geofenceRepository.findAll();
        }
        return ApiResponse.success("Geofences retrieved successfully", list);
    }
}
