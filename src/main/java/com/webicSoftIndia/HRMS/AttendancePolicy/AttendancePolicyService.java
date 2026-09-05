package com.webicSoftIndia.HRMS.AttendancePolicy;

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
public class AttendancePolicyService {

    private final AttendancePolicyRepository policyRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<AttendancePolicy> saveOrUpdatePolicy(AttendancePolicy policy) {
        if (policy == null) {
            return ApiResponse.error("AttendancePolicy payload cannot be null");
        }

        if (policy.getId() != null && policy.getId() > 0) {
            AttendancePolicy existing = policyRepository.findById(policy.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("AttendancePolicy not found with ID: " + policy.getId());
            }

            if (policy.getPolicyName() != null) {
                existing.setPolicyName(policy.getPolicyName());
            }
            if (policy.getGraceMinutes() != null) {
                existing.setGraceMinutes(policy.getGraceMinutes());
            }
            if (policy.getHalfDayHours() != null) {
                existing.setHalfDayHours(policy.getHalfDayHours());
            }
            if (policy.getFullDayHours() != null) {
                existing.setFullDayHours(policy.getFullDayHours());
            }
            if (policy.getLateMarkThreshold() != null) {
                existing.setLateMarkThreshold(policy.getLateMarkThreshold());
            }
            if (policy.getStatus() != null) {
                existing.setStatus(policy.getStatus());
            }
            if (policy.getIsActive() != null) {
                existing.setIsActive(policy.getIsActive());
            }

            AttendancePolicy updated = policyRepository.save(existing);
            return ApiResponse.success("AttendancePolicy updated successfully", updated);
        }

        if (policy.getPolicyCode() == null || policy.getPolicyCode().trim().isEmpty()) {
            return ApiResponse.error("Policy code is required");
        }
        if (policy.getPolicyName() == null || policy.getPolicyName().trim().isEmpty()) {
            return ApiResponse.error("Policy name is required");
        }

        if (policyRepository.findByPolicyCode(policy.getPolicyCode().trim()).isPresent()) {
            return ApiResponse.error("Policy code already exists");
        }

        if (policy.getStatus() == null) {
            policy.setStatus("ACTIVE");
        }
        if (policy.getIsActive() == null) {
            policy.setIsActive(true);
        }

        AttendancePolicy saved = policyRepository.save(policy);
        return ApiResponse.success("AttendancePolicy created successfully", saved);
    }

    @Transactional
    public ApiResponse<AttendancePolicy> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid AttendancePolicy ID is required");
        }
        AttendancePolicy policy = policyRepository.findById(id).orElse(null);
        if (policy == null) {
            return ApiResponse.error("AttendancePolicy not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(policy.getIsActive());
        policy.setIsActive(newStatus);
        policy.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        AttendancePolicy updated = policyRepository.save(policy);
        return ApiResponse.success("AttendancePolicy active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<AttendancePolicy>> getPoliciesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<AttendancePolicy> list;
        if (unitId != null && unitId > 0) {
            list = policyRepository.findByUnitId(unitId);
        } else {
            list = policyRepository.findAll();
        }
        return ApiResponse.success("Attendance policies retrieved successfully", list);
    }
}
