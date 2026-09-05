package com.webicSoftIndia.HRMS.EmployeeSalary;

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
public class EmployeeSalaryService {

    private final EmployeeSalaryRepository employeeSalaryRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<EmployeeSalary> saveOrUpdateEmployeeSalary(EmployeeSalary salary) {
        if (salary == null) {
            return ApiResponse.error("EmployeeSalary payload cannot be null");
        }

        if (salary.getId() != null && salary.getId() > 0) {
            EmployeeSalary existing = employeeSalaryRepository.findById(salary.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("EmployeeSalary not found with ID: " + salary.getId());
            }

            if (salary.getCtc() != null) existing.setCtc(salary.getCtc());
            if (salary.getGrossSalary() != null) existing.setGrossSalary(salary.getGrossSalary());
            if (salary.getNetSalary() != null) existing.setNetSalary(salary.getNetSalary());
            if (salary.getEffectiveFrom() != null) existing.setEffectiveFrom(salary.getEffectiveFrom());
            if (salary.getStatus() != null) existing.setStatus(salary.getStatus());
            if (salary.getIsActive() != null) existing.setIsActive(salary.getIsActive());

            EmployeeSalary updated = employeeSalaryRepository.save(existing);
            return ApiResponse.success("EmployeeSalary updated successfully", updated);
        }

        if (salary.getCtc() == null) {
            return ApiResponse.error("CTC amount is required");
        }

        if (salary.getStatus() == null) salary.setStatus("ACTIVE");
        if (salary.getIsActive() == null) salary.setIsActive(true);

        EmployeeSalary saved = employeeSalaryRepository.save(salary);
        return ApiResponse.success("EmployeeSalary created successfully", saved);
    }

    @Transactional
    public ApiResponse<EmployeeSalary> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid EmployeeSalary ID is required");
        }
        EmployeeSalary salary = employeeSalaryRepository.findById(id).orElse(null);
        if (salary == null) {
            return ApiResponse.error("EmployeeSalary not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(salary.getIsActive());
        salary.setIsActive(newStatus);
        salary.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        EmployeeSalary updated = employeeSalaryRepository.save(salary);
        return ApiResponse.success("EmployeeSalary active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<EmployeeSalary>> getEmployeeSalariesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<EmployeeSalary> list;
        if (unitId != null && unitId > 0) {
            list = employeeSalaryRepository.findByUnitId(unitId);
        } else {
            list = employeeSalaryRepository.findAll();
        }
        return ApiResponse.success("EmployeeSalaries retrieved successfully", list);
    }
}
