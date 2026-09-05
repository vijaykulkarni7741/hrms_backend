package com.webicSoftIndia.HRMS.NotificationHistory;

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
public class NotificationHistoryService {

    private final NotificationHistoryRepository notificationHistoryRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<NotificationHistory> saveOrUpdateNotificationHistory(NotificationHistory history) {
        if (history == null) {
            return ApiResponse.error("NotificationHistory payload cannot be null");
        }

        if (history.getId() != null && history.getId() > 0) {
            NotificationHistory existing = notificationHistoryRepository.findById(history.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("NotificationHistory not found with ID: " + history.getId());
            }

            if (history.getSentTo() != null) existing.setSentTo(history.getSentTo());
            if (history.getChannel() != null) existing.setChannel(history.getChannel());
            if (history.getDeliveryStatus() != null) existing.setDeliveryStatus(history.getDeliveryStatus());
            if (history.getFailureReason() != null) existing.setFailureReason(history.getFailureReason());
            if (history.getStatus() != null) existing.setStatus(history.getStatus());
            if (history.getIsActive() != null) existing.setIsActive(history.getIsActive());

            NotificationHistory updated = notificationHistoryRepository.save(existing);
            return ApiResponse.success("NotificationHistory updated successfully", updated);
        }

        if (history.getSentTo() == null || history.getSentTo().trim().isEmpty()) {
            return ApiResponse.error("SentTo address/phone is required");
        }

        if (history.getStatus() == null) history.setStatus("SENT");
        if (history.getIsActive() == null) history.setIsActive(true);

        NotificationHistory saved = notificationHistoryRepository.save(history);
        return ApiResponse.success("NotificationHistory created successfully", saved);
    }

    @Transactional
    public ApiResponse<NotificationHistory> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid NotificationHistory ID is required");
        }
        NotificationHistory history = notificationHistoryRepository.findById(id).orElse(null);
        if (history == null) {
            return ApiResponse.error("NotificationHistory not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(history.getIsActive());
        history.setIsActive(newStatus);
        history.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        NotificationHistory updated = notificationHistoryRepository.save(history);
        return ApiResponse.success("NotificationHistory active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<NotificationHistory>> getNotificationHistoriesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<NotificationHistory> list;
        if (unitId != null && unitId > 0) {
            list = notificationHistoryRepository.findByUnitId(unitId);
        } else {
            list = notificationHistoryRepository.findAll();
        }
        return ApiResponse.success("NotificationHistories retrieved successfully", list);
    }
}
