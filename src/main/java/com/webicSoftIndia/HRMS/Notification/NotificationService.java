package com.webicSoftIndia.HRMS.Notification;

import com.webicSoftIndia.HRMS.Employee.Employee;
import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.Organization.Organization;
import com.webicSoftIndia.HRMS.Unit.Unit;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserNotificationReadRepository userNotificationReadRepository;
    private final RedisSessionService redisSessionService;
    private final JdbcTemplate jdbcTemplate;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserNotificationReadRepository userNotificationReadRepository,
            RedisSessionService redisSessionService,
            JdbcTemplate jdbcTemplate) {
        this.notificationRepository = notificationRepository;
        this.userNotificationReadRepository = userNotificationReadRepository;
        this.redisSessionService = redisSessionService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void dropNotSupportConstraintsOnDatabaseStartup() {
        try {
            jdbcTemplate.execute("ALTER TABLE notifications ALTER COLUMN recipient_id DROP NOT NULL");
            jdbcTemplate.execute("ALTER TABLE notifications ALTER COLUMN organization_id DROP NOT NULL");
            jdbcTemplate.execute("ALTER TABLE notifications ALTER COLUMN unit_id DROP NOT NULL");
            log.info("Successfully dropped NOT NULL constraints on notifications table columns in database");
        } catch (Exception e) {
            log.warn("Notice when dropping notifications column constraints: {}", e.getMessage());
        }
    }

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
            if (notification.getScope() != null) existing.setScope(notification.getScope());
            if (notification.getCategory() != null) existing.setCategory(notification.getCategory());
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

        if (notification.getScope() == null) {
            notification.setScope(notification.getRecipient() != null ? "PRIVATE" : "BROADCAST");
        }
        if (notification.getStatus() == null) notification.setStatus("UNREAD");
        if (notification.getIsActive() == null) notification.setIsActive(true);

        Notification saved = notificationRepository.save(notification);
        return ApiResponse.success("Notification created successfully", saved);
    }

    @Transactional
    public ApiResponse<Notification> createBroadcastNotification(String title, String message, String category) {
        return createBroadcastNotification(title, message, category, null, null, null);
    }

    @Transactional
    public ApiResponse<Notification> createBroadcastNotification(String title, String message, String category, Unit unit, Organization organization) {
        return createBroadcastNotification(title, message, category, unit, organization, null);
    }

    @Transactional
    public ApiResponse<Notification> createBroadcastNotification(String title, String message, String category, Unit unit, Organization organization, Employee recipient) {
        Notification notification = Notification.builder()
                .organization(organization)
                .unit(unit)
                .recipient(recipient)
                .title(title)
                .message(message)
                .category(category != null ? category : "ANNOUNCEMENT")
                .type("BROADCAST")
                .scope("BROADCAST")
                .isRead(false)
                .status("UNREAD")
                .isActive(true)
                .build();
        Notification saved = notificationRepository.save(notification);
        return ApiResponse.success("Broadcast notification created successfully", saved);
    }

    @Transactional
    public ApiResponse<Void> markViewedAndDeleteForUser(Long notificationId, String sessionId) {
        if (notificationId == null || notificationId <= 0) {
            return ApiResponse.error("Valid Notification ID is required");
        }

        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null) {
            return ApiResponse.success("Notification already deleted", null);
        }

        Long empId = null;
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            EmployeeDto activeSession = redisSessionService.getSession(sessionId.trim());
            if (activeSession != null) {
                empId = activeSession.getId();
            }
        }

        // If private notification or specific recipient match
        if ("PRIVATE".equalsIgnoreCase(notification.getScope()) || (notification.getRecipient() != null && empId != null && empId.equals(notification.getRecipient().getId()))) {
            notificationRepository.deleteById(notificationId);
            log.info("Deleted private notification ID {} from database", notificationId);
            return ApiResponse.success("Private notification deleted from database", null);
        }

        // If broadcast notification
        if ("BROADCAST".equalsIgnoreCase(notification.getScope()) || notification.getRecipient() == null) {
            if (empId != null && empId > 0) {
                if (!userNotificationReadRepository.existsByNotificationIdAndEmployeeId(notificationId, empId)) {
                    UserNotificationRead read = UserNotificationRead.builder()
                            .notificationId(notificationId)
                            .employeeId(empId)
                            .build();
                    userNotificationReadRepository.save(read);
                }
                log.info("Marked broadcast notification ID {} as viewed/deleted for employee ID {}", notificationId, empId);
            } else {
                notificationRepository.deleteById(notificationId);
            }
            return ApiResponse.success("Notification marked as viewed and deleted for user", null);
        }

        notificationRepository.deleteById(notificationId);
        return ApiResponse.success("Notification deleted from database", null);
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

    @Transactional
    public void deleteNotification(Long id) {
        if (id != null && notificationRepository.existsById(id)) {
            notificationRepository.deleteById(id);
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Notification>> getNotificationsBySessionUnit(String sessionId) {
        List<Notification> list;
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            EmployeeDto userSession = redisSessionService.getSession(sessionId.trim());
            if (userSession != null && userSession.getId() != null) {
                Long empId = userSession.getId();
                Long unitId = userSession.getUnitId();

                List<Notification> rawList;
                if (unitId != null && unitId > 0) {
                    rawList = notificationRepository.findUserNotificationsByUnit(empId, unitId);
                } else {
                    rawList = notificationRepository.findByRecipientIdOrScopeOrderByCreatedAtDesc(empId, "BROADCAST");
                }

                // Exclude broadcast notifications already viewed/deleted by this user
                List<UserNotificationRead> readLogs = userNotificationReadRepository.findByEmployeeId(empId);
                Set<Long> readNotifIds = readLogs.stream()
                        .map(UserNotificationRead::getNotificationId)
                        .collect(Collectors.toSet());

                list = rawList.stream()
                        .filter(n -> !readNotifIds.contains(n.getId()))
                        .collect(Collectors.toList());

                return ApiResponse.success("User notifications retrieved successfully", list);
            }
        }
        list = notificationRepository.findAll();
        return ApiResponse.success("Notifications retrieved successfully", list != null ? list : Collections.emptyList());
    }
}
