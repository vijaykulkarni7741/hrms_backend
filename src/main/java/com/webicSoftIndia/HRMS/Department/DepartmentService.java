package com.webicSoftIndia.HRMS.Department;

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
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UnitRepository unitRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Department> saveOrUpdateDepartment(Department department) {
        if (department == null) {
            return ApiResponse.error("Department payload cannot be null");
        }

        // UPDATE MODE
        if (department.getId() != null && department.getId() > 0) {
            Department existing = departmentRepository.findById(department.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Department not found with ID: " + department.getId());
            }

            if (department.getDepartmentName() != null && !department.getDepartmentName().trim().isEmpty()) {
                existing.setDepartmentName(department.getDepartmentName().trim());
            }
            if (department.getStatus() != null) {
                existing.setStatus(department.getStatus());
            }
            if (department.getIsActive() != null) {
                existing.setIsActive(department.getIsActive());
            }
            if (department.getUnit() != null && department.getUnit().getId() != null) {
                Unit unit = unitRepository.findById(department.getUnit().getId()).orElse(null);
                if (unit != null) {
                    existing.setUnit(unit);
                }
            }

            Department updated = departmentRepository.save(existing);
            log.info("Updated Department ID: {}", updated.getId());
            return ApiResponse.success("Department updated successfully", updated);
        }

        // CREATE MODE
        if (department.getDepartmentCode() == null || department.getDepartmentCode().trim().isEmpty()) {
            return ApiResponse.error("Department code is required");
        }
        if (department.getDepartmentName() == null || department.getDepartmentName().trim().isEmpty()) {
            return ApiResponse.error("Department name is required");
        }

        if (departmentRepository.findByDepartmentCode(department.getDepartmentCode().trim()).isPresent()) {
            return ApiResponse.error("Department code already exists");
        }

        if (department.getUnit() != null && department.getUnit().getId() != null) {
            Unit unit = unitRepository.findById(department.getUnit().getId()).orElse(null);
            if (unit != null) {
                department.setUnit(unit);
            }
        }

        if (department.getStatus() == null) {
            department.setStatus("ACTIVE");
        }
        if (department.getIsActive() == null) {
            department.setIsActive(true);
        }

        Department saved = departmentRepository.save(department);
        log.info("Created Department ID: {}", saved.getId());
        return ApiResponse.success("Department created successfully", saved);
    }

    @Transactional
    public ApiResponse<Department> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Department ID is required");
        }
        Department department = departmentRepository.findById(id).orElse(null);
        if (department == null) {
            return ApiResponse.error("Department not found with ID: " + id);
        }

        boolean currentStatus = Boolean.TRUE.equals(department.getIsActive());
        boolean newStatus = !currentStatus;
        department.setIsActive(newStatus);
        department.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Department updated = departmentRepository.save(department);
        log.info("Toggled active status for Department ID: {} from {} to {}", id, currentStatus, newStatus);
        return ApiResponse.success("Department active status toggled successfully to " + newStatus, updated);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        if (id != null && departmentRepository.existsById(id)) {
            departmentRepository.deleteById(id);
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Department>> getDepartmentsBySessionUnit(String sessionId) {
        List<Department> list;
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
            if (sessionData != null && sessionData.getUnitId() != null && sessionData.getUnitId() > 0) {
                list = departmentRepository.findByUnitId(sessionData.getUnitId());
                return ApiResponse.success("Departments retrieved successfully", list);
            }
        }
        list = departmentRepository.findAll();
        return ApiResponse.success("Departments retrieved successfully", list);
    }
}

