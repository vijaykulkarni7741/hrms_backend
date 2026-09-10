package com.medimaze.HRMS.security;

import com.medimaze.HRMS.Employee.dto.EmployeeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSessionService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${application.security.jwt.session-ttl-seconds:86400}")
    private long sessionTtlSeconds;

    private static final String SESSION_KEY_PREFIX = "session:";

    public void saveSession(String sessionId, EmployeeDto employeeDto) {
        String key = SESSION_KEY_PREFIX + sessionId;
        try {
            redisTemplate.opsForValue().set(key, employeeDto, sessionTtlSeconds, TimeUnit.SECONDS);
            log.info("Saved session [{}] for employeeCode [{}] in Redis", sessionId, employeeDto.getEmployeeCode());
        } catch (Exception e) {
            log.error("Failed to save session [{}] in Redis: {}", sessionId, e.getMessage(), e);
        }
    }

    public EmployeeDto getSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return null;
        }
        String key = SESSION_KEY_PREFIX + sessionId;
        try {
            Object data = redisTemplate.opsForValue().get(key);
            if (data instanceof EmployeeDto) {
                return (EmployeeDto) data;
            } else if (data != null) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                return mapper.convertValue(data, EmployeeDto.class);
            }
        } catch (Exception e) {
            log.error("Failed to retrieve session [{}] from Redis: {}", sessionId, e.getMessage(), e);
        }
        return null;
    }

    public boolean hasSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }
        String key = SESSION_KEY_PREFIX + sessionId;
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Error checking session [{}] in Redis: {}", sessionId, e.getMessage(), e);
            return false;
        }
    }

    public void deleteSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return;
        }
        String key = SESSION_KEY_PREFIX + sessionId;
        try {
            redisTemplate.delete(key);
            log.info("Deleted session [{}] from Redis", sessionId);
        } catch (Exception e) {
            log.error("Failed to delete session [{}] from Redis: {}", sessionId, e.getMessage(), e);
        }
    }

    public long getSessionTtlSeconds() {
        return sessionTtlSeconds;
    }
}
