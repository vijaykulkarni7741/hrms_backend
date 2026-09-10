package com.medimaze.HRMS.ApprovalAuthority;

import com.medimaze.HRMS.Employee.dto.EmployeeDto;
import com.medimaze.HRMS.common.ApiResponse;
import com.medimaze.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalAuthorityService {

    private final ApprovalAuthorityRepository approvalAuthorityRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<ApprovalAuthority> saveOrUpdateApprovalAuthority(ApprovalAuthority authority) {
        if (authority == null) {
            return ApiResponse.error("ApprovalAuthority payload cannot be null");
        }

        if (authority.getId() != null && authority.getId() > 0) {
            ApprovalAuthority existing = approvalAuthorityRepository.findById(authority.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("ApprovalAuthority not found with ID: " + authority.getId());
            }

            if (authority.getAuthorityLevel() != null) existing.setAuthorityLevel(authority.getAuthorityLevel());
            if (authority.getModuleType() != null) existing.setModuleType(authority.getModuleType());
            if (authority.getStatus() != null) existing.setStatus(authority.getStatus());
            if (authority.getIsActive() != null) existing.setIsActive(authority.getIsActive());

            ApprovalAuthority updated = approvalAuthorityRepository.save(existing);
            return ApiResponse.success("ApprovalAuthority updated successfully", updated);
        }

        if (authority.getAuthorityLevel() == null) {
            return ApiResponse.error("Authority level is required");
        }

        if (authority.getStatus() == null) authority.setStatus("ACTIVE");
        if (authority.getIsActive() == null) authority.setIsActive(true);

        ApprovalAuthority saved = approvalAuthorityRepository.save(authority);
        return ApiResponse.success("ApprovalAuthority created successfully", saved);
    }

    @Transactional
    public ApiResponse<ApprovalAuthority> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid ApprovalAuthority ID is required");
        }
        ApprovalAuthority authority = approvalAuthorityRepository.findById(id).orElse(null);
        if (authority == null) {
            return ApiResponse.error("ApprovalAuthority not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(authority.getIsActive());
        authority.setIsActive(newStatus);
        authority.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        ApprovalAuthority updated = approvalAuthorityRepository.save(authority);
        return ApiResponse.success("ApprovalAuthority active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ApprovalAuthority>> getApprovalAuthoritiesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<ApprovalAuthority> list;
        if (unitId != null && unitId > 0) {
            list = approvalAuthorityRepository.findByUnitId(unitId);
        } else {
            list = approvalAuthorityRepository.findAll();
        }
        return ApiResponse.success("ApprovalAuthorities retrieved successfully", list);
    }
}
