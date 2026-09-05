package com.webicSoftIndia.HRMS.PayrollProcess;

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
public class PayrollProcessService {

    private final PayrollProcessRepository payrollProcessRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<PayrollProcess> saveOrUpdatePayrollProcess(PayrollProcess process) {
        if (process == null) {
            return ApiResponse.error("PayrollProcess payload cannot be null");
        }

        if (process.getId() != null && process.getId() > 0) {
            PayrollProcess existing = payrollProcessRepository.findById(process.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("PayrollProcess not found with ID: " + process.getId());
            }

            if (process.getProcessStep() != null) existing.setProcessStep(process.getProcessStep());
            if (process.getStatus() != null) existing.setStatus(process.getStatus());
            if (process.getIsActive() != null) existing.setIsActive(process.getIsActive());

            PayrollProcess updated = payrollProcessRepository.save(existing);
            return ApiResponse.success("PayrollProcess updated successfully", updated);
        }

        if (process.getProcessStep() == null || process.getProcessStep().trim().isEmpty()) {
            return ApiResponse.error("Process step is required");
        }

        if (process.getStatus() == null) process.setStatus("COMPLETED");
        if (process.getIsActive() == null) process.setIsActive(true);

        PayrollProcess saved = payrollProcessRepository.save(process);
        return ApiResponse.success("PayrollProcess created successfully", saved);
    }

    @Transactional
    public ApiResponse<PayrollProcess> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid PayrollProcess ID is required");
        }
        PayrollProcess process = payrollProcessRepository.findById(id).orElse(null);
        if (process == null) {
            return ApiResponse.error("PayrollProcess not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(process.getIsActive());
        process.setIsActive(newStatus);
        process.setStatus(newStatus ? "COMPLETED" : "FAILED");

        PayrollProcess updated = payrollProcessRepository.save(process);
        return ApiResponse.success("PayrollProcess active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<PayrollProcess>> getPayrollProcessesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<PayrollProcess> list;
        if (unitId != null && unitId > 0) {
            list = payrollProcessRepository.findByUnitId(unitId);
        } else {
            list = payrollProcessRepository.findAll();
        }
        return ApiResponse.success("PayrollProcesses retrieved successfully", list);
    }
}
