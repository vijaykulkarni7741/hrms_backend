package com.webicSoftIndia.HRMS.NotificationTemplate;

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
public class NotificationTemplateService {

    private final NotificationTemplateRepository notificationTemplateRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<NotificationTemplate> saveOrUpdateNotificationTemplate(NotificationTemplate template) {
        if (template == null) {
            return ApiResponse.error("NotificationTemplate payload cannot be null");
        }

        if (template.getId() != null && template.getId() > 0) {
            NotificationTemplate existing = notificationTemplateRepository.findById(template.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("NotificationTemplate not found with ID: " + template.getId());
            }

            if (template.getTemplateCode() != null) existing.setTemplateCode(template.getTemplateCode());
            if (template.getTemplateName() != null) existing.setTemplateName(template.getTemplateName());
            if (template.getSubject() != null) existing.setSubject(template.getSubject());
            if (template.getBody() != null) existing.setBody(template.getBody());
            if (template.getChannel() != null) existing.setChannel(template.getChannel());
            if (template.getStatus() != null) existing.setStatus(template.getStatus());
            if (template.getIsActive() != null) existing.setIsActive(template.getIsActive());

            NotificationTemplate updated = notificationTemplateRepository.save(existing);
            return ApiResponse.success("NotificationTemplate updated successfully", updated);
        }

        if (template.getTemplateCode() == null || template.getTemplateCode().trim().isEmpty()) {
            return ApiResponse.error("Template code is required");
        }
        if (template.getTemplateName() == null || template.getTemplateName().trim().isEmpty()) {
            return ApiResponse.error("Template name is required");
        }
        if (template.getBody() == null || template.getBody().trim().isEmpty()) {
            return ApiResponse.error("Body is required");
        }

        if (template.getStatus() == null) template.setStatus("ACTIVE");
        if (template.getIsActive() == null) template.setIsActive(true);

        NotificationTemplate saved = notificationTemplateRepository.save(template);
        return ApiResponse.success("NotificationTemplate created successfully", saved);
    }

    @Transactional
    public ApiResponse<NotificationTemplate> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid NotificationTemplate ID is required");
        }
        NotificationTemplate template = notificationTemplateRepository.findById(id).orElse(null);
        if (template == null) {
            return ApiResponse.error("NotificationTemplate not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(template.getIsActive());
        template.setIsActive(newStatus);
        template.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        NotificationTemplate updated = notificationTemplateRepository.save(template);
        return ApiResponse.success("NotificationTemplate active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<NotificationTemplate>> getNotificationTemplatesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<NotificationTemplate> list;
        if (unitId != null && unitId > 0) {
            list = notificationTemplateRepository.findByUnitId(unitId);
        } else {
            list = notificationTemplateRepository.findAll();
        }
        return ApiResponse.success("NotificationTemplates retrieved successfully", list);
    }
}
