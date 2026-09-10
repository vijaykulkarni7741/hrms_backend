package com.medimaze.HRMS.AuditLog;

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
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<AuditLog> saveOrUpdateAuditLog(AuditLog auditLog) {
        if (auditLog == null) {
            return ApiResponse.error("AuditLog payload cannot be null");
        }

        if (auditLog.getId() != null && auditLog.getId() > 0) {
            AuditLog existing = auditLogRepository.findById(auditLog.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("AuditLog not found with ID: " + auditLog.getId());
            }

            if (auditLog.getDescription() != null) {
                existing.setDescription(auditLog.getDescription());
            }

            AuditLog updated = auditLogRepository.save(existing);
            return ApiResponse.success("AuditLog updated successfully", updated);
        }

        if (auditLog.getAction() == null || auditLog.getAction().trim().isEmpty()) {
            return ApiResponse.error("Action is required");
        }
        if (auditLog.getModuleName() == null || auditLog.getModuleName().trim().isEmpty()) {
            return ApiResponse.error("Module name is required");
        }

        AuditLog saved = auditLogRepository.save(auditLog);
        return ApiResponse.success("AuditLog created successfully", saved);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<AuditLog>> getAuditLogsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<AuditLog> list = auditLogRepository.findAll();
        return ApiResponse.success("Audit logs retrieved successfully", list);
    }
}
