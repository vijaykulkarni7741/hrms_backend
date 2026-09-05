package com.webicSoftIndia.HRMS.Role;

import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.Unit.Unit;
import com.webicSoftIndia.HRMS.Unit.UnitRepository;
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
public class RoleService {

    private final RoleRepository roleRepository;
    private final UnitRepository unitRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Role> saveOrUpdateRole(Role role) {
        if (role == null) {
            return ApiResponse.error("Role payload cannot be null");
        }

        // UPDATE MODE
        if (role.getId() != null && role.getId() > 0) {
            Role existing = roleRepository.findById(role.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Role not found with ID: " + role.getId());
            }

            if (role.getRoleName() != null && !role.getRoleName().trim().isEmpty()) {
                existing.setRoleName(role.getRoleName().trim());
            }
            if (role.getDescription() != null) {
                existing.setDescription(role.getDescription());
            }
            if (role.getStatus() != null) {
                existing.setStatus(role.getStatus());
            }
            if (role.getIsActive() != null) {
                existing.setIsActive(role.getIsActive());
            }

            if (role.getUnit() != null && role.getUnit().getId() != null) {
                Unit unit = unitRepository.findById(role.getUnit().getId()).orElse(null);
                if (unit == null) {
                    return ApiResponse.error("Unit not found with ID: " + role.getUnit().getId());
                }
                existing.setUnit(unit);
            }

            Role updated = roleRepository.save(existing);
            log.info("Updated Role ID: {}", updated.getId());
            return ApiResponse.success("Role updated successfully", updated);
        }

        // CREATE MODE
        if (role.getRoleCode() == null || role.getRoleCode().trim().isEmpty()) {
            return ApiResponse.error("Role code is required");
        }
        if (role.getRoleName() == null || role.getRoleName().trim().isEmpty()) {
            return ApiResponse.error("Role name is required");
        }

        if (role.getUnit() != null && role.getUnit().getId() != null) {
            Unit unit = unitRepository.findById(role.getUnit().getId()).orElse(null);
            if (unit == null) {
                return ApiResponse.error("Associated Unit not found with ID: " + role.getUnit().getId());
            }
            role.setUnit(unit);

            if (roleRepository.findByUnitIdAndRoleCode(unit.getId(), role.getRoleCode().trim()).isPresent()) {
                return ApiResponse.error("Role code already exists for this Unit");
            }
        } else {
            if (roleRepository.findByRoleCode(role.getRoleCode().trim()).isPresent()) {
                return ApiResponse.error("Role code already exists");
            }
        }

        if (role.getStatus() == null) {
            role.setStatus("ACTIVE");
        }

        if (role.getIsActive() == null) {
            role.setIsActive(true);
        }

        Role saved = roleRepository.save(role);
        log.info("Created Role ID: {}", saved.getId());
        return ApiResponse.success("Role created successfully", saved);
    }

    @Transactional
    public ApiResponse<Role> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Role ID is required");
        }
        Role role = roleRepository.findById(id).orElse(null);
        if (role == null) {
            return ApiResponse.error("Role not found with ID: " + id);
        }

        boolean currentStatus = Boolean.TRUE.equals(role.getIsActive());
        boolean newStatus = !currentStatus;
        role.setIsActive(newStatus);
        role.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Role updated = roleRepository.save(role);
        log.info("Toggled active status for Role ID: {} from {} to {}", id, currentStatus, newStatus);
        return ApiResponse.success("Role active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Role>> getRolesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<Role> roles;
        if (unitId != null && unitId > 0) {
            roles = roleRepository.findByUnitId(unitId);
        } else {
            roles = roleRepository.findAll();
        }
        return ApiResponse.success("Roles retrieved successfully for unit ID: " + unitId, roles);
    }
}
