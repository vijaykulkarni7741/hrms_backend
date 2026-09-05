package com.webicSoftIndia.HRMS.ExitClearance;

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
public class ExitClearanceService {

    private final ExitClearanceRepository exitClearanceRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<ExitClearance> saveOrUpdateExitClearance(ExitClearance clearance) {
        if (clearance == null) {
            return ApiResponse.error("ExitClearance payload cannot be null");
        }

        if (clearance.getId() != null && clearance.getId() > 0) {
            ExitClearance existing = exitClearanceRepository.findById(clearance.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("ExitClearance record not found with ID: " + clearance.getId());
            }

            if (clearance.getDepartmentName() != null) existing.setDepartmentName(clearance.getDepartmentName());
            if (clearance.getRemarks() != null) existing.setRemarks(clearance.getRemarks());
            if (clearance.getClearanceStatus() != null) existing.setClearanceStatus(clearance.getClearanceStatus());
            if (clearance.getStatus() != null) existing.setStatus(clearance.getStatus());
            if (clearance.getIsActive() != null) existing.setIsActive(clearance.getIsActive());
            if (clearance.getClearedAt() != null) existing.setClearedAt(clearance.getClearedAt());

            ExitClearance updated = exitClearanceRepository.save(existing);
            return ApiResponse.success("ExitClearance updated successfully", updated);
        }

        if (clearance.getDepartmentName() == null || clearance.getDepartmentName().trim().isEmpty()) {
            return ApiResponse.error("Department name is required");
        }

        if (clearance.getClearanceStatus() == null) clearance.setClearanceStatus("PENDING");
        if (clearance.getStatus() == null) clearance.setStatus("ACTIVE");
        if (clearance.getIsActive() == null) clearance.setIsActive(true);

        ExitClearance saved = exitClearanceRepository.save(clearance);
        return ApiResponse.success("ExitClearance created successfully", saved);
    }

    @Transactional
    public ApiResponse<ExitClearance> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid ExitClearance ID is required");
        }
        ExitClearance clearance = exitClearanceRepository.findById(id).orElse(null);
        if (clearance == null) {
            return ApiResponse.error("ExitClearance record not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(clearance.getIsActive());
        clearance.setIsActive(newStatus);
        clearance.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        ExitClearance updated = exitClearanceRepository.save(clearance);
        return ApiResponse.success("ExitClearance active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ExitClearance>> getExitClearancesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<ExitClearance> list;
        if (unitId != null && unitId > 0) {
            list = exitClearanceRepository.findByUnitId(unitId);
        } else {
            list = exitClearanceRepository.findAll();
        }
        return ApiResponse.success("ExitClearances retrieved successfully", list);
    }
}
