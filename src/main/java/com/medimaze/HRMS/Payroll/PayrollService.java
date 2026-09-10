package com.medimaze.HRMS.Payroll;

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
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Payroll> saveOrUpdatePayroll(Payroll payroll) {
        if (payroll == null) {
            return ApiResponse.error("Payroll payload cannot be null");
        }

        if (payroll.getId() != null && payroll.getId() > 0) {
            Payroll existing = payrollRepository.findById(payroll.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Payroll not found with ID: " + payroll.getId());
            }

            if (payroll.getPayrollMonth() != null) existing.setPayrollMonth(payroll.getPayrollMonth());
            if (payroll.getPayrollYear() != null) existing.setPayrollYear(payroll.getPayrollYear());
            if (payroll.getTotalEmployees() != null) existing.setTotalEmployees(payroll.getTotalEmployees());
            if (payroll.getTotalAmount() != null) existing.setTotalAmount(payroll.getTotalAmount());
            if (payroll.getStatus() != null) existing.setStatus(payroll.getStatus());
            if (payroll.getIsActive() != null) existing.setIsActive(payroll.getIsActive());

            Payroll updated = payrollRepository.save(existing);
            return ApiResponse.success("Payroll updated successfully", updated);
        }

        if (payroll.getPayrollMonth() == null || payroll.getPayrollYear() == null) {
            return ApiResponse.error("Payroll month and year are required");
        }

        if (payroll.getStatus() == null) payroll.setStatus("DRAFT");
        if (payroll.getIsActive() == null) payroll.setIsActive(true);

        Payroll saved = payrollRepository.save(payroll);
        return ApiResponse.success("Payroll created successfully", saved);
    }

    @Transactional
    public ApiResponse<Payroll> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Payroll ID is required");
        }
        Payroll payroll = payrollRepository.findById(id).orElse(null);
        if (payroll == null) {
            return ApiResponse.error("Payroll not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(payroll.getIsActive());
        payroll.setIsActive(newStatus);
        payroll.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Payroll updated = payrollRepository.save(payroll);
        return ApiResponse.success("Payroll active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Payroll>> getPayrollsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<Payroll> list;
        if (unitId != null && unitId > 0) {
            list = payrollRepository.findByUnitId(unitId);
        } else {
            list = payrollRepository.findAll();
        }
        return ApiResponse.success("Payrolls retrieved successfully", list);
    }
}
