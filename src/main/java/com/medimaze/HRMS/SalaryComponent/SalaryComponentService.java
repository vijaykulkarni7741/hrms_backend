package com.medimaze.HRMS.SalaryComponent;

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
public class SalaryComponentService {

    private final SalaryComponentRepository salaryComponentRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<SalaryComponent> saveOrUpdateSalaryComponent(SalaryComponent component) {
        if (component == null) {
            return ApiResponse.error("SalaryComponent payload cannot be null");
        }

        if (component.getId() != null && component.getId() > 0) {
            SalaryComponent existing = salaryComponentRepository.findById(component.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("SalaryComponent not found with ID: " + component.getId());
            }

            if (component.getComponentName() != null) existing.setComponentName(component.getComponentName());
            if (component.getComponentType() != null) existing.setComponentType(component.getComponentType());
            if (component.getCalculationType() != null) existing.setCalculationType(component.getCalculationType());
            if (component.getDescription() != null) existing.setDescription(component.getDescription());
            if (component.getStatus() != null) existing.setStatus(component.getStatus());
            if (component.getIsActive() != null) existing.setIsActive(component.getIsActive());

            SalaryComponent updated = salaryComponentRepository.save(existing);
            return ApiResponse.success("SalaryComponent updated successfully", updated);
        }

        if (component.getComponentName() == null || component.getComponentName().trim().isEmpty()) {
            return ApiResponse.error("Component name is required");
        }

        if (component.getStatus() == null) component.setStatus("ACTIVE");
        if (component.getIsActive() == null) component.setIsActive(true);

        SalaryComponent saved = salaryComponentRepository.save(component);
        return ApiResponse.success("SalaryComponent created successfully", saved);
    }

    @Transactional
    public ApiResponse<SalaryComponent> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid SalaryComponent ID is required");
        }
        SalaryComponent component = salaryComponentRepository.findById(id).orElse(null);
        if (component == null) {
            return ApiResponse.error("SalaryComponent not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(component.getIsActive());
        component.setIsActive(newStatus);
        component.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        SalaryComponent updated = salaryComponentRepository.save(component);
        return ApiResponse.success("SalaryComponent active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<SalaryComponent>> getSalaryComponentsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<SalaryComponent> list;
        if (unitId != null && unitId > 0) {
            list = salaryComponentRepository.findByUnitId(unitId);
        } else {
            list = salaryComponentRepository.findAll();
        }
        return ApiResponse.success("SalaryComponents retrieved successfully", list);
    }
}
