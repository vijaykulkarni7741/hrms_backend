package com.webicSoftIndia.HRMS.Shift;

import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Shift> saveOrUpdateShift(Shift shift) {
        if (shift == null) {
            return ApiResponse.error("Shift payload cannot be null");
        }

        // UPDATE MODE
        if (shift.getId() != null && shift.getId() > 0) {
            Shift existing = shiftRepository.findById(shift.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Shift not found with ID: " + shift.getId());
            }

            if (shift.getShiftName() != null && !shift.getShiftName().trim().isEmpty()) {
                existing.setShiftName(shift.getShiftName().trim());
            }
            if (shift.getShiftType() != null) {
                existing.setShiftType(shift.getShiftType());
            }
            if (shift.getStartTime() != null) {
                existing.setStartTime(shift.getStartTime());
            }
            if (shift.getEndTime() != null) {
                existing.setEndTime(shift.getEndTime());
            }
            if (shift.getBreakStartTime() != null) {
                existing.setBreakStartTime(shift.getBreakStartTime());
            }
            if (shift.getBreakEndTime() != null) {
                existing.setBreakEndTime(shift.getBreakEndTime());
            }
            if (shift.getWorkingHours() != null) {
                existing.setWorkingHours(shift.getWorkingHours());
            }
            if (shift.getMinimumWorkingHours() != null) {
                existing.setMinimumWorkingHours(shift.getMinimumWorkingHours());
            }
            if (shift.getGraceTime() != null) {
                existing.setGraceTime(shift.getGraceTime());
            }
            if (shift.getLateMarkAfter() != null) {
                existing.setLateMarkAfter(shift.getLateMarkAfter());
            }
            if (shift.getHalfDayHours() != null) {
                existing.setHalfDayHours(shift.getHalfDayHours());
            }
            if (shift.getNightShift() != null) {
                existing.setNightShift(shift.getNightShift());
            }
            if (shift.getCrossDayShift() != null) {
                existing.setCrossDayShift(shift.getCrossDayShift());
            }
            if (shift.getOvertimeAllowed() != null) {
                existing.setOvertimeAllowed(shift.getOvertimeAllowed());
            }
            if (shift.getStatus() != null) {
                existing.setStatus(shift.getStatus());
            }
            if (shift.getIsActive() != null) {
                existing.setIsActive(shift.getIsActive());
            }

            Shift updated = shiftRepository.save(existing);
            log.info("Updated Shift ID: {}", updated.getId());
            return ApiResponse.success("Shift updated successfully", updated);
        }

        // CREATE MODE
        if (shift.getShiftCode() == null || shift.getShiftCode().trim().isEmpty()) {
            return ApiResponse.error("Shift code is required");
        }
        if (shift.getShiftName() == null || shift.getShiftName().trim().isEmpty()) {
            return ApiResponse.error("Shift name is required");
        }

        if (shiftRepository.findByShiftCode(shift.getShiftCode().trim()).isPresent()) {
            return ApiResponse.error("Shift code already exists");
        }

        if (shift.getStatus() == null) {
            shift.setStatus("ACTIVE");
        }
        if (shift.getIsActive() == null) {
            shift.setIsActive(true);
        }

        Shift saved = shiftRepository.save(shift);
        log.info("Created Shift ID: {}", saved.getId());
        return ApiResponse.success("Shift created successfully", saved);
    }

    @Transactional
    public ApiResponse<Shift> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Shift ID is required");
        }
        Shift shift = shiftRepository.findById(id).orElse(null);
        if (shift == null) {
            return ApiResponse.error("Shift not found with ID: " + id);
        }

        boolean currentStatus = Boolean.TRUE.equals(shift.getIsActive());
        boolean newStatus = !currentStatus;
        shift.setIsActive(newStatus);
        shift.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Shift updated = shiftRepository.save(shift);
        log.info("Toggled active status for Shift ID: {} to {}", id, newStatus);
        return ApiResponse.success("Shift active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Shift>> getShiftsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<Shift> list;
        if (unitId != null && unitId > 0) {
            list = shiftRepository.findByUnitId(unitId);
        } else {
            list = shiftRepository.findAll();
        }
        return ApiResponse.success("Shifts retrieved successfully", list);
    }
}

