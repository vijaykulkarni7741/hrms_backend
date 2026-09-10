package com.medimaze.HRMS.TaxRule;

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
public class TaxRuleService {

    private final TaxRuleRepository taxRuleRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<TaxRule> saveOrUpdateTaxRule(TaxRule rule) {
        if (rule == null) {
            return ApiResponse.error("TaxRule payload cannot be null");
        }

        if (rule.getId() != null && rule.getId() > 0) {
            TaxRule existing = taxRuleRepository.findById(rule.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("TaxRule not found with ID: " + rule.getId());
            }

            if (rule.getRuleName() != null) existing.setRuleName(rule.getRuleName());
            if (rule.getFinancialYear() != null) existing.setFinancialYear(rule.getFinancialYear());
            if (rule.getMinIncome() != null) existing.setMinIncome(rule.getMinIncome());
            if (rule.getMaxIncome() != null) existing.setMaxIncome(rule.getMaxIncome());
            if (rule.getTaxPercentage() != null) existing.setTaxPercentage(rule.getTaxPercentage());
            if (rule.getStatus() != null) existing.setStatus(rule.getStatus());
            if (rule.getIsActive() != null) existing.setIsActive(rule.getIsActive());

            TaxRule updated = taxRuleRepository.save(existing);
            return ApiResponse.success("TaxRule updated successfully", updated);
        }

        if (rule.getRuleName() == null || rule.getRuleName().trim().isEmpty()) {
            return ApiResponse.error("Rule name is required");
        }

        if (rule.getStatus() == null) rule.setStatus("ACTIVE");
        if (rule.getIsActive() == null) rule.setIsActive(true);

        TaxRule saved = taxRuleRepository.save(rule);
        return ApiResponse.success("TaxRule created successfully", saved);
    }

    @Transactional
    public ApiResponse<TaxRule> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid TaxRule ID is required");
        }
        TaxRule rule = taxRuleRepository.findById(id).orElse(null);
        if (rule == null) {
            return ApiResponse.error("TaxRule not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(rule.getIsActive());
        rule.setIsActive(newStatus);
        rule.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        TaxRule updated = taxRuleRepository.save(rule);
        return ApiResponse.success("TaxRule active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<TaxRule>> getTaxRulesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<TaxRule> list;
        if (unitId != null && unitId > 0) {
            list = taxRuleRepository.findByUnitId(unitId);
        } else {
            list = taxRuleRepository.findAll();
        }
        return ApiResponse.success("TaxRules retrieved successfully", list);
    }
}
