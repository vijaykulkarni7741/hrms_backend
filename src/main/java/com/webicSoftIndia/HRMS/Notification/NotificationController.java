package com.webicSoftIndia.HRMS.Notification;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.common.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/notifications", "/v1/notifications"})
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AutoNotificationScheduler autoNotificationScheduler;

    @PostMapping({"/trigger-auto", "/auto-generate"})
    public ResponseEntity<ApiResponse<String>> triggerAutoNotifications() {
        autoNotificationScheduler.generateDailyAutomatedNotifications();
        return ResponseEntity.ok(ApiResponse.success("Automated Birthday & Work Anniversary notifications triggered successfully", "OK"));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Notification>> createNotification(@RequestBody Notification notification) {
        ApiResponse<Notification> response = notificationService.saveOrUpdateNotification(notification);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<Notification>> updateNotification(@RequestBody Notification notification) {
        ApiResponse<Notification> response = notificationService.saveOrUpdateNotification(notification);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping({"/delete", "/mark-viewed", "/read", "/view"})
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam,
            @RequestBody IdRequest request) {
        
        Long id = request != null ? request.getId() : null;
        if (id != null) {
            String targetSessionId = extractSessionId(xSessionId, sessionIdHeader, sessionIdParam);
            ApiResponse<Void> response = notificationService.markViewedAndDeleteForUser(id, targetSessionId);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("Valid ID is required for deletion"));
    }

    @PostMapping({"/create-broadcast", "/broadcast"})
    public ResponseEntity<ApiResponse<Notification>> createBroadcastNotification(@RequestBody Notification notification) {
        if (notification == null || notification.getTitle() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Notification payload is required"));
        }
        ApiResponse<Notification> response = notificationService.createBroadcastNotification(
                notification.getTitle(), notification.getMessage(), notification.getCategory());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/toggle-active")
    public ResponseEntity<ApiResponse<Notification>> toggleActiveStatus(@RequestBody IdRequest request) {
        Long id = request != null ? request.getId() : null;
        ApiResponse<Notification> response = notificationService.toggleActiveStatus(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<Notification>>> listNotifications(
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {

        String targetSessionId = extractSessionId(xSessionId, sessionIdHeader, sessionIdParam);
        ApiResponse<List<Notification>> response = notificationService.getNotificationsBySessionUnit(targetSessionId);
        return ResponseEntity.ok(response);
    }

    private String extractSessionId(String xSessionId, String sessionIdHeader, String sessionIdParam) {
        if (xSessionId != null && !xSessionId.trim().isEmpty()) return xSessionId.trim();
        if (sessionIdHeader != null && !sessionIdHeader.trim().isEmpty()) return sessionIdHeader.trim();
        if (sessionIdParam != null && !sessionIdParam.trim().isEmpty()) return sessionIdParam.trim();
        return null;
    }
}

