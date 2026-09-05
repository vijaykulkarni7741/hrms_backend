package com.webicSoftIndia.HRMS.LoginHistory;

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
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<LoginHistory> saveOrUpdateLoginHistory(LoginHistory loginHistory) {
        if (loginHistory == null) {
            return ApiResponse.error("LoginHistory payload cannot be null");
        }

        if (loginHistory.getId() != null && loginHistory.getId() > 0) {
            LoginHistory existing = loginHistoryRepository.findById(loginHistory.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("LoginHistory not found with ID: " + loginHistory.getId());
            }

            if (loginHistory.getStatus() != null) {
                existing.setStatus(loginHistory.getStatus());
            }
            if (loginHistory.getFailureReason() != null) {
                existing.setFailureReason(loginHistory.getFailureReason());
            }

            LoginHistory updated = loginHistoryRepository.save(existing);
            return ApiResponse.success("LoginHistory updated successfully", updated);
        }

        if (loginHistory.getUsername() == null || loginHistory.getUsername().trim().isEmpty()) {
            return ApiResponse.error("Username is required");
        }
        if (loginHistory.getStatus() == null) {
            loginHistory.setStatus("SUCCESS");
        }

        LoginHistory saved = loginHistoryRepository.save(loginHistory);
        return ApiResponse.success("LoginHistory created successfully", saved);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<LoginHistory>> getLoginHistoryBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<LoginHistory> list = loginHistoryRepository.findAll();
        return ApiResponse.success("Login history retrieved successfully", list);
    }
}
