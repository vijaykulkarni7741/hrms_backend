package com.medimaze.HRMS.PushNotification;

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
public class PushNotificationService {

    private final PushNotificationRepository pushNotificationRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<PushNotification> saveOrUpdatePushNotification(PushNotification notification) {
        if (notification == null) {
            return ApiResponse.error("PushNotification payload cannot be null");
        }

        if (notification.getId() != null && notification.getId() > 0) {
            PushNotification existing = pushNotificationRepository.findById(notification.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("PushNotification not found with ID: " + notification.getId());
            }

            if (notification.getDeviceToken() != null) existing.setDeviceToken(notification.getDeviceToken());
            if (notification.getTitle() != null) existing.setTitle(notification.getTitle());
            if (notification.getBody() != null) existing.setBody(notification.getBody());
            if (notification.getPayloadJson() != null) existing.setPayloadJson(notification.getPayloadJson());
            if (notification.getStatus() != null) existing.setStatus(notification.getStatus());
            if (notification.getIsActive() != null) existing.setIsActive(notification.getIsActive());
            if (notification.getSentAt() != null) existing.setSentAt(notification.getSentAt());

            PushNotification updated = pushNotificationRepository.save(existing);
            return ApiResponse.success("PushNotification updated successfully", updated);
        }

        if (notification.getDeviceToken() == null || notification.getDeviceToken().trim().isEmpty()) {
            return ApiResponse.error("Device token is required");
        }
        if (notification.getTitle() == null || notification.getTitle().trim().isEmpty()) {
            return ApiResponse.error("Title is required");
        }
        if (notification.getBody() == null || notification.getBody().trim().isEmpty()) {
            return ApiResponse.error("Body is required");
        }

        if (notification.getStatus() == null) notification.setStatus("PENDING");
        if (notification.getIsActive() == null) notification.setIsActive(true);

        PushNotification saved = pushNotificationRepository.save(notification);
        return ApiResponse.success("PushNotification created successfully", saved);
    }

    @Transactional
    public ApiResponse<PushNotification> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid PushNotification ID is required");
        }
        PushNotification notification = pushNotificationRepository.findById(id).orElse(null);
        if (notification == null) {
            return ApiResponse.error("PushNotification not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(notification.getIsActive());
        notification.setIsActive(newStatus);
        notification.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        PushNotification updated = pushNotificationRepository.save(notification);
        return ApiResponse.success("PushNotification active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<PushNotification>> getPushNotificationsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<PushNotification> list;
        if (unitId != null && unitId > 0) {
            list = pushNotificationRepository.findByUnitId(unitId);
        } else {
            list = pushNotificationRepository.findAll();
        }
        return ApiResponse.success("PushNotifications retrieved successfully", list);
    }
}
