package com.webicSoftIndia.HRMS.PasswordHistory;

import com.webicSoftIndia.HRMS.User.User;
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
public class PasswordHistoryService {

    private final PasswordHistoryRepository passwordHistoryRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public PasswordHistory recordPasswordHistory(User user, String passwordHash, String changedBy) {
        if (user == null || passwordHash == null || passwordHash.trim().isEmpty()) {
            log.warn("Cannot record password history: null user or passwordHash");
            return null;
        }

        PasswordHistory history = PasswordHistory.builder()
                .user(user)
                .passwordHash(passwordHash.trim())
                .changedBy(changedBy != null ? changedBy.trim() : (user.getUsername() != null ? user.getUsername() : "SYSTEM"))
                .build();

        PasswordHistory saved = passwordHistoryRepository.save(history);
        log.info("Recorded PasswordHistory ID: {} for User ID: {}", saved.getId(), user.getId());
        return saved;
    }

    @Transactional
    public ApiResponse<PasswordHistory> saveOrUpdatePasswordHistory(PasswordHistory passwordHistory) {
        if (passwordHistory == null) {
            return ApiResponse.error("PasswordHistory payload cannot be null");
        }

        if (passwordHistory.getId() != null && passwordHistory.getId() > 0) {
            PasswordHistory existing = passwordHistoryRepository.findById(passwordHistory.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("PasswordHistory not found with ID: " + passwordHistory.getId());
            }

            if (passwordHistory.getPasswordHash() != null) {
                existing.setPasswordHash(passwordHistory.getPasswordHash());
            }

            PasswordHistory updated = passwordHistoryRepository.save(existing);
            return ApiResponse.success("PasswordHistory updated successfully", updated);
        }

        if (passwordHistory.getPasswordHash() == null || passwordHistory.getPasswordHash().trim().isEmpty()) {
            return ApiResponse.error("Password hash is required");
        }

        PasswordHistory saved = passwordHistoryRepository.save(passwordHistory);
        return ApiResponse.success("PasswordHistory created successfully", saved);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<PasswordHistory>> getPasswordHistoryBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<PasswordHistory> list = passwordHistoryRepository.findAll();
        return ApiResponse.success("Password history retrieved successfully", list);
    }
}
