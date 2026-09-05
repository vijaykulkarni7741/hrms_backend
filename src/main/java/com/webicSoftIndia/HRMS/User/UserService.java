package com.webicSoftIndia.HRMS.User;

import com.webicSoftIndia.HRMS.PasswordHistory.PasswordHistoryService;
import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RedisSessionService redisSessionService;
    private final PasswordHistoryService passwordHistoryService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ApiResponse<User> saveOrUpdateUser(User user) {
        if (user == null) {
            return ApiResponse.error("User payload cannot be null");
        }

        boolean passwordUpdated = false;

        if (user.getId() != null && user.getId() > 0) {
            User existing = userRepository.findById(user.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("User not found with ID: " + user.getId());
            }
            if (user.getEmail() != null) {
                existing.setEmail(user.getEmail());
            }
            if (user.getStatus() != null) {
                existing.setStatus(user.getStatus());
            }
            if (user.getIsActive() != null) {
                existing.setIsActive(user.getIsActive());
            }
            if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                String rawPassword = user.getPassword().trim();
                String encodedPassword = rawPassword.startsWith("$2a$") || rawPassword.startsWith("$2b$")
                        ? rawPassword
                        : passwordEncoder.encode(rawPassword);
                existing.setPassword(encodedPassword);
                passwordUpdated = true;
            }

            User updated = userRepository.save(existing);

            if (passwordUpdated && updated.getPassword() != null) {
                passwordHistoryService.recordPasswordHistory(
                        updated,
                        updated.getPassword(),
                        user.getUpdatedBy() != null ? user.getUpdatedBy() : updated.getUsername()
                );
            }

            log.info("Updated User ID: {}", updated.getId());
            return ApiResponse.success("User updated successfully", updated);
        }

        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return ApiResponse.error("Username is required");
        }

        if (userRepository.findByUsername(user.getUsername().trim()).isPresent()) {
            return ApiResponse.error("Username already exists");
        }

        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            String rawPassword = user.getPassword().trim();
            String encodedPassword = rawPassword.startsWith("$2a$") || rawPassword.startsWith("$2b$")
                    ? rawPassword
                    : passwordEncoder.encode(rawPassword);
            user.setPassword(encodedPassword);
            passwordUpdated = true;
        } else {
            user.setPassword(passwordEncoder.encode("Password@123"));
            passwordUpdated = true;
        }

        if (user.getStatus() == null) {
            user.setStatus("ACTIVE");
        }
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }

        User saved = userRepository.save(user);

        if (passwordUpdated && saved.getPassword() != null) {
            passwordHistoryService.recordPasswordHistory(
                    saved,
                    saved.getPassword(),
                    user.getCreatedBy() != null ? user.getCreatedBy() : saved.getUsername()
            );
        }

        log.info("Created User ID: {}", saved.getId());
        return ApiResponse.success("User created successfully", saved);
    }

    @Transactional
    public ApiResponse<User> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid User ID is required");
        }
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(user.getIsActive());
        user.setIsActive(newStatus);
        user.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        User updated = userRepository.save(user);
        log.info("Toggled active status for User ID: {} to {}", id, newStatus);
        return ApiResponse.success("User active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<User>> getUsersBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<User> list = userRepository.findAll();
        return ApiResponse.success("Users retrieved successfully", list);
    }
}
