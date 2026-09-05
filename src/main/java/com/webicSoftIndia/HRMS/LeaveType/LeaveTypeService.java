package com.webicSoftIndia.HRMS.LeaveType;

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
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<LeaveType> saveOrUpdateLeaveType(LeaveType leaveType) {
        if (leaveType == null) {
            return ApiResponse.error("LeaveType payload cannot be null");
        }

        if (leaveType.getId() != null && leaveType.getId() > 0) {
            LeaveType existing = leaveTypeRepository.findById(leaveType.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("LeaveType not found with ID: " + leaveType.getId());
            }

            if (leaveType.getTypeName() != null) existing.setTypeName(leaveType.getTypeName());
            if (leaveType.getTypeCode() != null) existing.setTypeCode(leaveType.getTypeCode());
            if (leaveType.getDescription() != null) existing.setDescription(leaveType.getDescription());
            if (leaveType.getIsPaid() != null) existing.setIsPaid(leaveType.getIsPaid());
            if (leaveType.getStatus() != null) existing.setStatus(leaveType.getStatus());
            if (leaveType.getIsActive() != null) existing.setIsActive(leaveType.getIsActive());

            LeaveType updated = leaveTypeRepository.save(existing);
            return ApiResponse.success("LeaveType updated successfully", updated);
        }

        if (leaveType.getTypeName() == null || leaveType.getTypeName().trim().isEmpty()) {
            return ApiResponse.error("Type name is required");
        }

        if (leaveType.getStatus() == null) leaveType.setStatus("ACTIVE");
        if (leaveType.getIsActive() == null) leaveType.setIsActive(true);

        LeaveType saved = leaveTypeRepository.save(leaveType);
        return ApiResponse.success("LeaveType created successfully", saved);
    }

    @Transactional
    public ApiResponse<LeaveType> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid LeaveType ID is required");
        }
        LeaveType leaveType = leaveTypeRepository.findById(id).orElse(null);
        if (leaveType == null) {
            return ApiResponse.error("LeaveType not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(leaveType.getIsActive());
        leaveType.setIsActive(newStatus);
        leaveType.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        LeaveType updated = leaveTypeRepository.save(leaveType);
        return ApiResponse.success("LeaveType active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<LeaveType>> getLeaveTypesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<LeaveType> list;
        if (unitId != null && unitId > 0) {
            list = leaveTypeRepository.findByUnitId(unitId);
        } else {
            list = leaveTypeRepository.findAll();
        }
        return ApiResponse.success("LeaveTypes retrieved successfully", list);
    }
}
