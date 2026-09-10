package com.medimaze.HRMS.UserPermission;

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
public class UserPermissionService {

    private final UserPermissionRepository userPermissionRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<UserPermission> saveOrUpdateUserPermission(UserPermission userPermission) {
        if (userPermission == null) {
            return ApiResponse.error("UserPermission payload cannot be null");
        }

        if (userPermission.getId() != null && userPermission.getId() > 0) {
            UserPermission existing = userPermissionRepository.findById(userPermission.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("UserPermission not found with ID: " + userPermission.getId());
            }

            if (userPermission.getIsGranted() != null) {
                existing.setIsGranted(userPermission.getIsGranted());
            }
            if (userPermission.getStatus() != null) {
                existing.setStatus(userPermission.getStatus());
            }
            if (userPermission.getIsActive() != null) {
                existing.setIsActive(userPermission.getIsActive());
            }

            UserPermission updated = userPermissionRepository.save(existing);
            return ApiResponse.success("UserPermission updated successfully", updated);
        }

        if (userPermission.getStatus() == null) {
            userPermission.setStatus("ACTIVE");
        }
        if (userPermission.getIsActive() == null) {
            userPermission.setIsActive(true);
        }

        UserPermission saved = userPermissionRepository.save(userPermission);
        return ApiResponse.success("UserPermission created successfully", saved);
    }

    @Transactional
    public ApiResponse<UserPermission> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid UserPermission ID is required");
        }
        UserPermission userPermission = userPermissionRepository.findById(id).orElse(null);
        if (userPermission == null) {
            return ApiResponse.error("UserPermission not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(userPermission.getIsActive());
        userPermission.setIsActive(newStatus);
        userPermission.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        UserPermission updated = userPermissionRepository.save(userPermission);
        return ApiResponse.success("UserPermission active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<UserPermission>> getUserPermissionsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<UserPermission> list = userPermissionRepository.findAll();
        return ApiResponse.success("UserPermissions retrieved successfully", list);
    }
}
