package com.webicSoftIndia.HRMS.AdvanceSalary;

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
public class AdvanceSalaryService {

    private final AdvanceSalaryRepository advanceSalaryRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<AdvanceSalary> saveOrUpdateAdvanceSalary(AdvanceSalary advance) {
        if (advance == null) {
            return ApiResponse.error("AdvanceSalary payload cannot be null");
        }

        if (advance.getId() != null && advance.getId() > 0) {
            AdvanceSalary existing = advanceSalaryRepository.findById(advance.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("AdvanceSalary not found with ID: " + advance.getId());
            }

            if (advance.getRequestedAmount() != null) existing.setRequestedAmount(advance.getRequestedAmount());
            if (advance.getApprovedAmount() != null) existing.setApprovedAmount(advance.getApprovedAmount());
            if (advance.getReason() != null) existing.setReason(advance.getReason());
            if (advance.getDisbursedDate() != null) existing.setDisbursedDate(advance.getDisbursedDate());
            if (advance.getStatus() != null) existing.setStatus(advance.getStatus());
            if (advance.getIsActive() != null) existing.setIsActive(advance.getIsActive());

            AdvanceSalary updated = advanceSalaryRepository.save(existing);
            return ApiResponse.success("AdvanceSalary updated successfully", updated);
        }

        if (advance.getRequestedAmount() == null) {
            return ApiResponse.error("Requested amount is required");
        }

        if (advance.getStatus() == null) advance.setStatus("PENDING");
        if (advance.getIsActive() == null) advance.setIsActive(true);

        AdvanceSalary saved = advanceSalaryRepository.save(advance);
        return ApiResponse.success("AdvanceSalary created successfully", saved);
    }

    @Transactional
    public ApiResponse<AdvanceSalary> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid AdvanceSalary ID is required");
        }
        AdvanceSalary advance = advanceSalaryRepository.findById(id).orElse(null);
        if (advance == null) {
            return ApiResponse.error("AdvanceSalary not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(advance.getIsActive());
        advance.setIsActive(newStatus);
        advance.setStatus(newStatus ? "PENDING" : "CANCELLED");

        AdvanceSalary updated = advanceSalaryRepository.save(advance);
        return ApiResponse.success("AdvanceSalary active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<AdvanceSalary>> getAdvanceSalariesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<AdvanceSalary> list;
        if (unitId != null && unitId > 0) {
            list = advanceSalaryRepository.findByUnitId(unitId);
        } else {
            list = advanceSalaryRepository.findAll();
        }
        return ApiResponse.success("AdvanceSalaries retrieved successfully", list);
    }
}
