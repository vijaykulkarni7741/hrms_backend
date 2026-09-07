package com.webicSoftIndia.HRMS.auth;

import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.auth.dto.LoginRequest;
import com.webicSoftIndia.HRMS.auth.dto.LoginResponse;
import com.webicSoftIndia.HRMS.auth.dto.TenantBootstrapRequest;
import com.webicSoftIndia.HRMS.auth.dto.TenantBootstrapResponse;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/auth", "/v1/auth"})
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RedisSessionService redisSessionService;

    @PostMapping({"/bootstrap", "/initialize-tenant"})
    public ResponseEntity<ApiResponse<TenantBootstrapResponse>> bootstrapTenant(@RequestBody TenantBootstrapRequest request) {
        ApiResponse<TenantBootstrapResponse> response = authService.bootstrapTenant(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        ApiResponse<LoginResponse> response = authService.login(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/session")
    public ResponseEntity<ApiResponse<EmployeeDto>> getSessionData(
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {

        String targetSessionId = null;
        if (xSessionId != null && !xSessionId.trim().isEmpty()) {
            targetSessionId = xSessionId.trim();
        } else if (sessionIdHeader != null && !sessionIdHeader.trim().isEmpty()) {
            targetSessionId = sessionIdHeader.trim();
        } else if (sessionIdParam != null && !sessionIdParam.trim().isEmpty()) {
            targetSessionId = sessionIdParam.trim();
        }

        ApiResponse<EmployeeDto> response = authService.getSessionData(targetSessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader(value = "X-Session-ID", required = false) String sessionIdHeader) {
        if (sessionIdHeader != null && !sessionIdHeader.isEmpty()) {
            redisSessionService.deleteSession(sessionIdHeader);
        }
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}
