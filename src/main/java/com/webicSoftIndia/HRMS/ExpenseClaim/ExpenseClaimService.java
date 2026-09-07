package com.webicSoftIndia.HRMS.ExpenseClaim;

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
public class ExpenseClaimService {

    private final ExpenseClaimRepository expenseClaimRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<ExpenseClaim> saveOrUpdateExpenseClaim(ExpenseClaim claim) {
        if (claim == null) {
            return ApiResponse.error("ExpenseClaim payload cannot be null");
        }

        if (claim.getId() != null && claim.getId() > 0) {
            ExpenseClaim existing = expenseClaimRepository.findById(claim.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("ExpenseClaim record not found with ID: " + claim.getId());
            }

            if (claim.getExpenseCategory() != null) existing.setExpenseCategory(claim.getExpenseCategory());
            if (claim.getAmount() != null) existing.setAmount(claim.getAmount());
            if (claim.getClaimDate() != null) existing.setClaimDate(claim.getClaimDate());
            if (claim.getReceiptUrl() != null) existing.setReceiptUrl(claim.getReceiptUrl());
            if (claim.getDescription() != null) existing.setDescription(claim.getDescription());
            if (claim.getStatus() != null) existing.setStatus(claim.getStatus());
            if (claim.getIsActive() != null) existing.setIsActive(claim.getIsActive());

            ExpenseClaim updated = expenseClaimRepository.save(existing);
            return ApiResponse.success("ExpenseClaim updated successfully", updated);
        }

        if (claim.getExpenseCategory() == null || claim.getExpenseCategory().trim().isEmpty()) {
            return ApiResponse.error("Expense category is required");
        }
        if (claim.getAmount() == null) {
            return ApiResponse.error("Amount is required");
        }

        if (claim.getStatus() == null) claim.setStatus("SUBMITTED");
        if (claim.getIsActive() == null) claim.setIsActive(true);

        ExpenseClaim saved = expenseClaimRepository.save(claim);
        return ApiResponse.success("ExpenseClaim created successfully", saved);
    }

    @Transactional
    public ApiResponse<ExpenseClaim> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid ExpenseClaim ID is required");
        }
        ExpenseClaim claim = expenseClaimRepository.findById(id).orElse(null);
        if (claim == null) {
            return ApiResponse.error("ExpenseClaim record not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(claim.getIsActive());
        claim.setIsActive(newStatus);
        claim.setStatus(newStatus ? "SUBMITTED" : "INACTIVE");

        ExpenseClaim updated = expenseClaimRepository.save(claim);
        return ApiResponse.success("ExpenseClaim active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ExpenseClaim>> getExpenseClaimsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.success("ExpenseClaims retrieved successfully", expenseClaimRepository.findAll());
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.success("ExpenseClaims retrieved successfully", expenseClaimRepository.findAll());
        }

        Long unitId = sessionData.getUnitId();
        List<ExpenseClaim> list;
        if (unitId != null && unitId > 0) {
            list = expenseClaimRepository.findByUnitId(unitId);
        } else {
            list = expenseClaimRepository.findAll();
        }
        return ApiResponse.success("ExpenseClaims retrieved successfully", list);
    }
}
