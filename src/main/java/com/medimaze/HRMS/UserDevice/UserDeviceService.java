package com.medimaze.HRMS.UserDevice;

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
public class UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<UserDevice> saveOrUpdateUserDevice(UserDevice userDevice) {
        if (userDevice == null) {
            return ApiResponse.error("UserDevice payload cannot be null");
        }

        if (userDevice.getId() != null && userDevice.getId() > 0) {
            UserDevice existing = userDeviceRepository.findById(userDevice.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("UserDevice not found with ID: " + userDevice.getId());
            }

            if (userDevice.getDeviceName() != null) {
                existing.setDeviceName(userDevice.getDeviceName());
            }
            if (userDevice.getIsTrusted() != null) {
                existing.setIsTrusted(userDevice.getIsTrusted());
            }
            if (userDevice.getStatus() != null) {
                existing.setStatus(userDevice.getStatus());
            }
            if (userDevice.getIsActive() != null) {
                existing.setIsActive(userDevice.getIsActive());
            }

            UserDevice updated = userDeviceRepository.save(existing);
            return ApiResponse.success("UserDevice updated successfully", updated);
        }

        if (userDevice.getStatus() == null) {
            userDevice.setStatus("ACTIVE");
        }
        if (userDevice.getIsActive() == null) {
            userDevice.setIsActive(true);
        }

        UserDevice saved = userDeviceRepository.save(userDevice);
        return ApiResponse.success("UserDevice created successfully", saved);
    }

    @Transactional
    public ApiResponse<UserDevice> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid UserDevice ID is required");
        }
        UserDevice userDevice = userDeviceRepository.findById(id).orElse(null);
        if (userDevice == null) {
            return ApiResponse.error("UserDevice not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(userDevice.getIsActive());
        userDevice.setIsActive(newStatus);
        userDevice.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        UserDevice updated = userDeviceRepository.save(userDevice);
        return ApiResponse.success("UserDevice active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<UserDevice>> getUserDevicesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<UserDevice> list = userDeviceRepository.findAll();
        return ApiResponse.success("UserDevices retrieved successfully", list);
    }
}
