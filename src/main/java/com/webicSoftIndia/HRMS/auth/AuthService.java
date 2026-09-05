package com.webicSoftIndia.HRMS.auth;

import com.webicSoftIndia.HRMS.Employee.Employee;
import com.webicSoftIndia.HRMS.Employee.EmployeeRepository;
import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.User.User;
import com.webicSoftIndia.HRMS.User.UserRepository;
import com.webicSoftIndia.HRMS.UserSession.UserSessionService;
import com.webicSoftIndia.HRMS.auth.dto.LoginRequest;
import com.webicSoftIndia.HRMS.auth.dto.LoginResponse;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.JwtService;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final UserSessionService userSessionService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        String identifier = request.getEmployeeIdentifier();
        if (identifier == null || identifier.trim().isEmpty()) {
            return ApiResponse.error("Employee ID / Username or code is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ApiResponse.error("Password is required");
        }

        identifier = identifier.trim();

        // 1. First search for User by username or email
        Optional<User> userOpt = userRepository.findByUsername(identifier);
        if (!userOpt.isPresent()) {
            userOpt = userRepository.findByEmail(identifier);
        }

        Employee employee = null;
        if (userOpt.isPresent()) {
            employee = userOpt.get().getEmployee();
        } else {
            // 2. If User not found directly, search Employee by code, officialEmail, email, or numeric ID
            Optional<Employee> employeeOpt = employeeRepository.findByEmployeeCode(identifier);
            if (!employeeOpt.isPresent()) {
                employeeOpt = employeeRepository.findByOfficialEmail(identifier);
            }
            if (!employeeOpt.isPresent()) {
                employeeOpt = employeeRepository.findByEmail(identifier);
            }
            if (!employeeOpt.isPresent()) {
                try {
                    Long numericId = Long.parseLong(identifier);
                    employeeOpt = employeeRepository.findById(numericId);
                } catch (NumberFormatException ignored) {
                }
            }

            if (employeeOpt.isPresent()) {
                employee = employeeOpt.get();
                userOpt = userRepository.findByEmployeeId(employee.getId());
            }
        }

        if (!userOpt.isPresent()) {
            log.warn("Login failed: User/Employee not found with identifier [{}]", identifier);
            return ApiResponse.error("Invalid Employee ID/Code/Username or Password");
        }

        User user = userOpt.get();
        if (employee == null) {
            employee = user.getEmployee();
        }

        if (Boolean.FALSE.equals(user.getIsActive()) || (employee != null && Boolean.FALSE.equals(employee.getIsActive()))) {
            log.warn("Login failed: User/Employee [{}] account is inactive", identifier);
            return ApiResponse.error("Account is inactive");
        }

        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            log.warn("Login failed: User [{}] account is locked", user.getUsername());
            return ApiResponse.error("Account is locked due to multiple failed login attempts");
        }

        String storedPassword = user.getPassword();
        boolean passwordMatches = storedPassword != null && passwordEncoder.matches(request.getPassword(), storedPassword);

        if (!passwordMatches) {
            log.warn("Login failed: Invalid password for User [{}]", user.getUsername());
            int failedAttempts = (user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0) + 1;
            user.setFailedLoginAttempts(failedAttempts);
            if (failedAttempts >= 5) {
                user.setAccountLocked(true);
                user.setLockTime(LocalDateTime.now());
                log.warn("User [{}] account has been locked", user.getUsername());
            }
            userRepository.save(user);
            return ApiResponse.error("Invalid Employee ID/Code/Username or Password");
        }

        // Reset failed login attempts on success
        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String sessionId = UUID.randomUUID().toString();
        String usernameOrCode = user.getUsername() != null ? user.getUsername() : (employee != null ? employee.getEmployeeCode() : identifier);
        Long empId = employee != null ? employee.getId() : user.getId();

        String token = jwtService.generateToken(
                usernameOrCode,
                empId,
                sessionId
        );

        // Store combined User + Employee details into Redis Session
        EmployeeDto employeeDto = EmployeeDto.fromEntity(employee, user);
        employeeDto.setToken(token);
        redisSessionService.saveSession(sessionId, employeeDto);

        // Automatically record session entry in security_user_sessions DB table
        LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(jwtService.getJwtExpiration());
        userSessionService.createUserSession(user, sessionId, token, null, null, expiryTime);

        LoginResponse loginResponse = LoginResponse.builder()
                .token(token)
                .sessionId(sessionId)
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpiration())
                .build();

        log.info("Login successful for user [{}] with sessionId [{}]", usernameOrCode, sessionId);
        return ApiResponse.success("Login successful", loginResponse);
    }

    public ApiResponse<EmployeeDto> getSessionData(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID is required");
        }
        EmployeeDto employeeDto = redisSessionService.getSession(sessionId.trim());
        if (employeeDto == null) {
            return ApiResponse.error("Session not found or expired in Redis");
        }
        return ApiResponse.success("Session data retrieved successfully from Redis", employeeDto);
    }
}
