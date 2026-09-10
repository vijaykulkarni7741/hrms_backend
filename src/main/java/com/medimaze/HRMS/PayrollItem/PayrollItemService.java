package com.medimaze.HRMS.PayrollItem;

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
public class PayrollItemService {

    private final PayrollItemRepository payrollItemRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<PayrollItem> saveOrUpdatePayrollItem(PayrollItem item) {
        if (item == null) {
            return ApiResponse.error("PayrollItem payload cannot be null");
        }

        if (item.getId() != null && item.getId() > 0) {
            PayrollItem existing = payrollItemRepository.findById(item.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("PayrollItem not found with ID: " + item.getId());
            }

            if (item.getGrossEarnings() != null) existing.setGrossEarnings(item.getGrossEarnings());
            if (item.getTotalDeductions() != null) existing.setTotalDeductions(item.getTotalDeductions());
            if (item.getNetPay() != null) existing.setNetPay(item.getNetPay());
            if (item.getStatus() != null) existing.setStatus(item.getStatus());
            if (item.getIsActive() != null) existing.setIsActive(item.getIsActive());

            PayrollItem updated = payrollItemRepository.save(existing);
            return ApiResponse.success("PayrollItem updated successfully", updated);
        }

        if (item.getStatus() == null) item.setStatus("PROCESSED");
        if (item.getIsActive() == null) item.setIsActive(true);

        PayrollItem saved = payrollItemRepository.save(item);
        return ApiResponse.success("PayrollItem created successfully", saved);
    }

    @Transactional
    public ApiResponse<PayrollItem> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid PayrollItem ID is required");
        }
        PayrollItem item = payrollItemRepository.findById(id).orElse(null);
        if (item == null) {
            return ApiResponse.error("PayrollItem not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(item.getIsActive());
        item.setIsActive(newStatus);
        item.setStatus(newStatus ? "PROCESSED" : "CANCELLED");

        PayrollItem updated = payrollItemRepository.save(item);
        return ApiResponse.success("PayrollItem active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<PayrollItem>> getPayrollItemsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<PayrollItem> list;
        if (unitId != null && unitId > 0) {
            list = payrollItemRepository.findByUnitId(unitId);
        } else {
            list = payrollItemRepository.findAll();
        }
        return ApiResponse.success("PayrollItems retrieved successfully", list);
    }
}
