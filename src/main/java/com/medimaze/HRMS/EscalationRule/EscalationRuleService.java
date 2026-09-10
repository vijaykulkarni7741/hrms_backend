package com.medimaze.HRMS.EscalationRule;

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
public class EscalationRuleService {

    private final EscalationRuleRepository escalationRuleRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<EscalationRule> saveOrUpdateEscalationRule(EscalationRule rule) {
        if (rule == null) {
            return ApiResponse.error("EscalationRule payload cannot be null");
        }

        if (rule.getId() != null && rule.getId() > 0) {
            EscalationRule existing = escalationRuleRepository.findById(rule.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("EscalationRule not found with ID: " + rule.getId());
            }

            if (rule.getEscalationHours() != null) existing.setEscalationHours(rule.getEscalationHours());
            if (rule.getEscalateToRole() != null) existing.setEscalateToRole(rule.getEscalateToRole());
            if (rule.getStatus() != null) existing.setStatus(rule.getStatus());
            if (rule.getIsActive() != null) existing.setIsActive(rule.getIsActive());

            EscalationRule updated = escalationRuleRepository.save(existing);
            return ApiResponse.success("EscalationRule updated successfully", updated);
        }

        if (rule.getEscalateToRole() == null || rule.getEscalateToRole().trim().isEmpty()) {
            return ApiResponse.error("Escalate to role is required");
        }

        if (rule.getStatus() == null) rule.setStatus("ACTIVE");
        if (rule.getIsActive() == null) rule.setIsActive(true);

        EscalationRule saved = escalationRuleRepository.save(rule);
        return ApiResponse.success("EscalationRule created successfully", saved);
    }

    @Transactional
    public ApiResponse<EscalationRule> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid EscalationRule ID is required");
        }
        EscalationRule rule = escalationRuleRepository.findById(id).orElse(null);
        if (rule == null) {
            return ApiResponse.error("EscalationRule not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(rule.getIsActive());
        rule.setIsActive(newStatus);
        rule.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        EscalationRule updated = escalationRuleRepository.save(rule);
        return ApiResponse.success("EscalationRule active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<EscalationRule>> getEscalationRulesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<EscalationRule> list;
        if (unitId != null && unitId > 0) {
            list = escalationRuleRepository.findByUnitId(unitId);
        } else {
            list = escalationRuleRepository.findAll();
        }
        return ApiResponse.success("EscalationRules retrieved successfully", list);
    }
}
