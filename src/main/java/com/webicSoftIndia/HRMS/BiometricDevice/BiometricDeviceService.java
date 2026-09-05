package com.webicSoftIndia.HRMS.BiometricDevice;

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
public class BiometricDeviceService {

    private final BiometricDeviceRepository deviceRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<BiometricDevice> saveOrUpdateDevice(BiometricDevice device) {
        if (device == null) {
            return ApiResponse.error("BiometricDevice payload cannot be null");
        }

        if (device.getId() != null && device.getId() > 0) {
            BiometricDevice existing = deviceRepository.findById(device.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("BiometricDevice not found with ID: " + device.getId());
            }

            if (device.getDeviceName() != null) {
                existing.setDeviceName(device.getDeviceName());
            }
            if (device.getSerialNumber() != null) {
                existing.setSerialNumber(device.getSerialNumber());
            }
            if (device.getIpAddress() != null) {
                existing.setIpAddress(device.getIpAddress());
            }
            if (device.getPort() != null) {
                existing.setPort(device.getPort());
            }
            if (device.getLocation() != null) {
                existing.setLocation(device.getLocation());
            }
            if (device.getStatus() != null) {
                existing.setStatus(device.getStatus());
            }
            if (device.getIsActive() != null) {
                existing.setIsActive(device.getIsActive());
            }

            BiometricDevice updated = deviceRepository.save(existing);
            return ApiResponse.success("BiometricDevice updated successfully", updated);
        }

        if (device.getDeviceCode() == null || device.getDeviceCode().trim().isEmpty()) {
            return ApiResponse.error("Device code is required");
        }
        if (device.getDeviceName() == null || device.getDeviceName().trim().isEmpty()) {
            return ApiResponse.error("Device name is required");
        }

        if (deviceRepository.findByDeviceCode(device.getDeviceCode().trim()).isPresent()) {
            return ApiResponse.error("Device code already exists");
        }

        if (device.getStatus() == null) {
            device.setStatus("ACTIVE");
        }
        if (device.getIsActive() == null) {
            device.setIsActive(true);
        }

        BiometricDevice saved = deviceRepository.save(device);
        return ApiResponse.success("BiometricDevice created successfully", saved);
    }

    @Transactional
    public ApiResponse<BiometricDevice> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid BiometricDevice ID is required");
        }
        BiometricDevice device = deviceRepository.findById(id).orElse(null);
        if (device == null) {
            return ApiResponse.error("BiometricDevice not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(device.getIsActive());
        device.setIsActive(newStatus);
        device.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        BiometricDevice updated = deviceRepository.save(device);
        return ApiResponse.success("BiometricDevice active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<BiometricDevice>> getDevicesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<BiometricDevice> list;
        if (unitId != null && unitId > 0) {
            list = deviceRepository.findByUnitId(unitId);
        } else {
            list = deviceRepository.findAll();
        }
        return ApiResponse.success("Biometric devices retrieved successfully", list);
    }
}
