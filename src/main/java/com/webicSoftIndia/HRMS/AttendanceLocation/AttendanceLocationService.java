package com.webicSoftIndia.HRMS.AttendanceLocation;

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
public class AttendanceLocationService {

    private final AttendanceLocationRepository locationRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<AttendanceLocation> saveOrUpdateLocation(AttendanceLocation location) {
        if (location == null) {
            return ApiResponse.error("AttendanceLocation payload cannot be null");
        }

        if (location.getId() != null && location.getId() > 0) {
            AttendanceLocation existing = locationRepository.findById(location.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("AttendanceLocation not found with ID: " + location.getId());
            }

            if (location.getLocationName() != null) {
                existing.setLocationName(location.getLocationName());
            }
            if (location.getAddress() != null) {
                existing.setAddress(location.getAddress());
            }
            if (location.getLatitude() != null) {
                existing.setLatitude(location.getLatitude());
            }
            if (location.getLongitude() != null) {
                existing.setLongitude(location.getLongitude());
            }
            if (location.getRadiusMeters() != null) {
                existing.setRadiusMeters(location.getRadiusMeters());
            }
            if (location.getStatus() != null) {
                existing.setStatus(location.getStatus());
            }
            if (location.getIsActive() != null) {
                existing.setIsActive(location.getIsActive());
            }

            AttendanceLocation updated = locationRepository.save(existing);
            return ApiResponse.success("AttendanceLocation updated successfully", updated);
        }

        if (location.getLocationName() == null || location.getLocationName().trim().isEmpty()) {
            return ApiResponse.error("Location name is required");
        }

        if (location.getStatus() == null) {
            location.setStatus("ACTIVE");
        }
        if (location.getIsActive() == null) {
            location.setIsActive(true);
        }

        AttendanceLocation saved = locationRepository.save(location);
        return ApiResponse.success("AttendanceLocation created successfully", saved);
    }

    @Transactional
    public ApiResponse<AttendanceLocation> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid AttendanceLocation ID is required");
        }
        AttendanceLocation location = locationRepository.findById(id).orElse(null);
        if (location == null) {
            return ApiResponse.error("AttendanceLocation not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(location.getIsActive());
        location.setIsActive(newStatus);
        location.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        AttendanceLocation updated = locationRepository.save(location);
        return ApiResponse.success("AttendanceLocation active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<AttendanceLocation>> getLocationsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<AttendanceLocation> list;
        if (unitId != null && unitId > 0) {
            list = locationRepository.findByUnitId(unitId);
        } else {
            list = locationRepository.findAll();
        }
        return ApiResponse.success("Attendance locations retrieved successfully", list);
    }
}
