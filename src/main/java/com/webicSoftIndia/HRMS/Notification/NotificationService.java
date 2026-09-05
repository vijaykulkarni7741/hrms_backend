package com.webicSoftIndia.HRMS.Notification;

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
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Notification> saveOrUpdateNotification(Notification notification) {
        if (notification == null) {
            return ApiResponse.error("Notification payload cannot be null");
        }

        if (notification.getId() != null && notification.getId() > 0) {
            Notification existing = notificationRepository.findById(notification.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Notification not found with ID: " + notification.getId());
            }

            if (notification.getTitle() != null) existing.setTitle(notification.getTitle());
            if (notification.getMessage() != null) existing.setMessage(notification.getMessage());
            if (notification.getType() != null) existing.setType(notification.getType());
            if (notification.getIsRead() != null) existing.setIsRead(notification.getIsRead());
            if (notification.getReadAt() != null) existing.setReadAt(notification.getReadAt());
            if (notification.getStatus() != null) existing.setStatus(notification.getStatus());
            if (notification.getIsActive() != null) existing.setIsActive(notification.getIsActive());

            Notification updated = notificationRepository.save(existing);
            return ApiResponse.success("Notification updated successfully", updated);
        }

        if (notification.getTitle() == null || notification.getTitle().trim().isEmpty()) {
            return ApiResponse.error("Title is required");
        }
        if (notification.getMessage() == null || notification.getMessage().trim().isEmpty()) {
            return ApiResponse.error("Message is required");
        }

        if (notification.getStatus() == null) notification.setStatus("UNREAD");
        if (notification.getIsActive() == null) notification.setIsActive(true);

        Notification saved = notificationRepository.save(notification);
        return ApiResponse.success("Notification created successfully", saved);
    }

    @Transactional
    public ApiResponse<Notification> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Notification ID is required");
        }
        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification == null) {
            return ApiResponse.error("Notification not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(notification.getIsActive());
        notification.setIsActive(newStatus);
        notification.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Notification updated = notificationRepository.save(notification);
        return ApiResponse.success("Notification active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Notification>> getNotificationsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<Notification> list;
        if (unitId != null && unitId > 0) {
            list = notificationRepository.findByUnitId(unitId);
        } else {
            list = notificationRepository.findAll();
        }
        return ApiResponse.success("Notifications retrieved successfully", list);
    }
}
