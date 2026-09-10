package com.medimaze.HRMS.RolePermission;

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
public class RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<RolePermission> saveOrUpdateRolePermission(RolePermission rolePermission) {
        if (rolePermission == null) {
            return ApiResponse.error("RolePermission payload cannot be null");
        }

        if (rolePermission.getId() != null && rolePermission.getId() > 0) {
            RolePermission existing = rolePermissionRepository.findById(rolePermission.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("RolePermission not found with ID: " + rolePermission.getId());
            }

            if (rolePermission.getStatus() != null) {
                existing.setStatus(rolePermission.getStatus());
            }
            if (rolePermission.getIsActive() != null) {
                existing.setIsActive(rolePermission.getIsActive());
            }

            RolePermission updated = rolePermissionRepository.save(existing);
            return ApiResponse.success("RolePermission updated successfully", updated);
        }

        if (rolePermission.getStatus() == null) {
            rolePermission.setStatus("ACTIVE");
        }
        if (rolePermission.getIsActive() == null) {
            rolePermission.setIsActive(true);
        }

        RolePermission saved = rolePermissionRepository.save(rolePermission);
        return ApiResponse.success("RolePermission created successfully", saved);
    }

    @Transactional
    public ApiResponse<RolePermission> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid RolePermission ID is required");
        }
        RolePermission rolePermission = rolePermissionRepository.findById(id).orElse(null);
        if (rolePermission == null) {
            return ApiResponse.error("RolePermission not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(rolePermission.getIsActive());
        rolePermission.setIsActive(newStatus);
        rolePermission.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        RolePermission updated = rolePermissionRepository.save(rolePermission);
        return ApiResponse.success("RolePermission active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<RolePermission>> getRolePermissionsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<RolePermission> list = rolePermissionRepository.findAll();
        return ApiResponse.success("RolePermissions retrieved successfully", list);
    }
}
