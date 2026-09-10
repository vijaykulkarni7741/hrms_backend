package com.medimaze.HRMS.Permission;

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
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Permission> saveOrUpdatePermission(Permission permission) {
        if (permission == null) {
            return ApiResponse.error("Permission payload cannot be null");
        }

        if (permission.getId() != null && permission.getId() > 0) {
            Permission existing = permissionRepository.findById(permission.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Permission not found with ID: " + permission.getId());
            }

            if (permission.getPermissionName() != null) {
                existing.setPermissionName(permission.getPermissionName());
            }
            if (permission.getModuleName() != null) {
                existing.setModuleName(permission.getModuleName());
            }
            if (permission.getDescription() != null) {
                existing.setDescription(permission.getDescription());
            }
            if (permission.getStatus() != null) {
                existing.setStatus(permission.getStatus());
            }
            if (permission.getIsActive() != null) {
                existing.setIsActive(permission.getIsActive());
            }

            Permission updated = permissionRepository.save(existing);
            log.info("Updated Permission ID: {}", updated.getId());
            return ApiResponse.success("Permission updated successfully", updated);
        }

        if (permission.getPermissionCode() == null || permission.getPermissionCode().trim().isEmpty()) {
            return ApiResponse.error("Permission code is required");
        }
        if (permission.getPermissionName() == null || permission.getPermissionName().trim().isEmpty()) {
            return ApiResponse.error("Permission name is required");
        }

        if (permissionRepository.findByPermissionCode(permission.getPermissionCode().trim()).isPresent()) {
            return ApiResponse.error("Permission code already exists");
        }

        if (permission.getStatus() == null) {
            permission.setStatus("ACTIVE");
        }
        if (permission.getIsActive() == null) {
            permission.setIsActive(true);
        }

        Permission saved = permissionRepository.save(permission);
        log.info("Created Permission ID: {}", saved.getId());
        return ApiResponse.success("Permission created successfully", saved);
    }

    @Transactional
    public ApiResponse<Permission> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Permission ID is required");
        }
        Permission permission = permissionRepository.findById(id).orElse(null);
        if (permission == null) {
            return ApiResponse.error("Permission not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(permission.getIsActive());
        permission.setIsActive(newStatus);
        permission.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Permission updated = permissionRepository.save(permission);
        log.info("Toggled active status for Permission ID: {} to {}", id, newStatus);
        return ApiResponse.success("Permission active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Permission>> getPermissionsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<Permission> list = permissionRepository.findAll();
        return ApiResponse.success("Permissions retrieved successfully", list);
    }
}
