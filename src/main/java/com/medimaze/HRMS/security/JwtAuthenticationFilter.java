package com.medimaze.HRMS.security;

import com.medimaze.HRMS.Employee.dto.EmployeeDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RedisSessionService redisSessionService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Extract session ID from possible header keys
        String headerSessionId = request.getHeader("X-Session-ID");
        if (headerSessionId == null || headerSessionId.trim().isEmpty()) {
            headerSessionId = request.getHeader("Session-ID");
        }
        if (headerSessionId == null || headerSessionId.trim().isEmpty()) {
            headerSessionId = request.getHeader("sessionId");
        }

        if (authHeader != null && authHeader.startsWith("Bearer ") && headerSessionId != null && !headerSessionId.trim().isEmpty()) {
            String jwt = authHeader.substring(7).trim();
            headerSessionId = headerSessionId.trim();

            try {
                String employeeCode = jwtService.extractSubject(jwt);
                String jwtSessionId = jwtService.extractSessionId(jwt);

                if (headerSessionId.equals(jwtSessionId)) {
                    boolean hasActiveSession = redisSessionService.hasSession(headerSessionId);
                    EmployeeDto employeeDto = redisSessionService.getSession(headerSessionId);
                    boolean isJwtValid = jwtService.isTokenValid(jwt, employeeCode);
                    boolean isStoredTokenMatching = employeeDto != null && (employeeDto.getToken() == null || jwt.equals(employeeDto.getToken()));

                    if (hasActiveSession && isJwtValid && employeeDto != null && isStoredTokenMatching) {
                        setSecurityContext(request, employeeDto);
                        log.debug("Successfully authenticated request for employee [{}] with session [{}]", employeeDto.getEmployeeCode(), headerSessionId);
                    }
                }
            } catch (Exception e) {
                log.warn("JWT/Session authentication error: {}", e.getMessage());
            }
        } else if (headerSessionId != null && !headerSessionId.trim().isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Direct Session-ID authentication fallback via Redis
            try {
                EmployeeDto employeeDto = redisSessionService.getSession(headerSessionId.trim());
                if (employeeDto != null) {
                    setSecurityContext(request, employeeDto);
                    log.debug("Session-only authentication successful for employee [{}]", employeeDto.getEmployeeCode());
                }
            } catch (Exception e) {
                log.warn("Session ID authentication error: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setSecurityContext(HttpServletRequest request, EmployeeDto employeeDto) {
        String principalName = employeeDto.getEmployeeCode() != null ? employeeDto.getEmployeeCode() : "USER";
        String roleName = employeeDto.getRoleCode() != null ? employeeDto.getRoleCode() : "ROLE_USER";

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                principalName,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(roleName))
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
