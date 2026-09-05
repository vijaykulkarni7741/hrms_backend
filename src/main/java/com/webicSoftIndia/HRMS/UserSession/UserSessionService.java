package com.webicSoftIndia.HRMS.UserSession;

import com.webicSoftIndia.HRMS.User.User;
import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public UserSession createUserSession(User user, String sessionId, String token, String ipAddress, String userAgent, LocalDateTime expiryTime) {
        if (user == null || sessionId == null || sessionId.trim().isEmpty()) {
            log.warn("Cannot create UserSession: null user or empty sessionId");
            return null;
        }

        UserSession userSession = UserSession.builder()
                .sessionId(sessionId.trim())
                .user(user)
                .token(token)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loginTime(LocalDateTime.now())
                .lastActivityTime(LocalDateTime.now())
                .expiryTime(expiryTime != null ? expiryTime : LocalDateTime.now().plusDays(1))
                .status("ACTIVE")
                .isActive(true)
                .build();

        UserSession saved = userSessionRepository.save(userSession);
        log.info("Recorded UserSession ID: {} for sessionId [{}] and User ID: {}", saved.getId(), sessionId, user.getId());
        return saved;
    }

    @Transactional
    public ApiResponse<UserSession> saveOrUpdateUserSession(UserSession userSession) {
        if (userSession == null) {
            return ApiResponse.error("UserSession payload cannot be null");
        }

        if (userSession.getId() != null && userSession.getId() > 0) {
            UserSession existing = userSessionRepository.findById(userSession.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("UserSession not found with ID: " + userSession.getId());
            }

            if (userSession.getLastActivityTime() != null) {
                existing.setLastActivityTime(userSession.getLastActivityTime());
            }
            if (userSession.getExpiryTime() != null) {
                existing.setExpiryTime(userSession.getExpiryTime());
            }
            if (userSession.getStatus() != null) {
                existing.setStatus(userSession.getStatus());
            }
            if (userSession.getIsActive() != null) {
                existing.setIsActive(userSession.getIsActive());
            }

            UserSession updated = userSessionRepository.save(existing);
            return ApiResponse.success("UserSession updated successfully", updated);
        }

        if (userSession.getSessionId() == null || userSession.getSessionId().trim().isEmpty()) {
            return ApiResponse.error("Session ID is required");
        }

        if (userSessionRepository.findBySessionId(userSession.getSessionId().trim()).isPresent()) {
            return ApiResponse.error("Session ID already exists");
        }

        if (userSession.getStatus() == null) {
            userSession.setStatus("ACTIVE");
        }
        if (userSession.getIsActive() == null) {
            userSession.setIsActive(true);
        }

        UserSession saved = userSessionRepository.save(userSession);
        return ApiResponse.success("UserSession created successfully", saved);
    }

    @Transactional
    public ApiResponse<UserSession> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid UserSession ID is required");
        }
        UserSession userSession = userSessionRepository.findById(id).orElse(null);
        if (userSession == null) {
            return ApiResponse.error("UserSession not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(userSession.getIsActive());
        userSession.setIsActive(newStatus);
        userSession.setStatus(newStatus ? "ACTIVE" : "EXPIRED");

        UserSession updated = userSessionRepository.save(userSession);
        return ApiResponse.success("UserSession active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<UserSession>> getUserSessionsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<UserSession> list = userSessionRepository.findAll();
        return ApiResponse.success("UserSessions retrieved successfully", list);
    }
}
