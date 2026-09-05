package com.webicSoftIndia.HRMS.Holiday;

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
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Holiday> saveOrUpdateHoliday(Holiday holiday) {
        if (holiday == null) {
            return ApiResponse.error("Holiday payload cannot be null");
        }

        // UPDATE MODE
        if (holiday.getId() != null && holiday.getId() > 0) {
            Holiday existing = holidayRepository.findById(holiday.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Holiday not found with ID: " + holiday.getId());
            }

            if (holiday.getHolidayName() != null && !holiday.getHolidayName().trim().isEmpty()) {
                existing.setHolidayName(holiday.getHolidayName().trim());
            }
            if (holiday.getHolidayDate() != null) {
                existing.setHolidayDate(holiday.getHolidayDate());
            }
            if (holiday.getHolidayType() != null) {
                existing.setHolidayType(holiday.getHolidayType());
            }
            if (holiday.getIsOptional() != null) {
                existing.setIsOptional(holiday.getIsOptional());
            }
            if (holiday.getDescription() != null) {
                existing.setDescription(holiday.getDescription());
            }
            if (holiday.getStatus() != null) {
                existing.setStatus(holiday.getStatus());
            }
            if (holiday.getIsActive() != null) {
                existing.setIsActive(holiday.getIsActive());
            }

            Holiday updated = holidayRepository.save(existing);
            log.info("Updated Holiday ID: {}", updated.getId());
            return ApiResponse.success("Holiday updated successfully", updated);
        }

        // CREATE MODE
        if (holiday.getHolidayCode() == null || holiday.getHolidayCode().trim().isEmpty()) {
            return ApiResponse.error("Holiday code is required");
        }
        if (holiday.getHolidayName() == null || holiday.getHolidayName().trim().isEmpty()) {
            return ApiResponse.error("Holiday name is required");
        }

        if (holidayRepository.findByHolidayCode(holiday.getHolidayCode().trim()).isPresent()) {
            return ApiResponse.error("Holiday code already exists");
        }

        if (holiday.getStatus() == null) {
            holiday.setStatus("ACTIVE");
        }
        if (holiday.getIsActive() == null) {
            holiday.setIsActive(true);
        }

        Holiday saved = holidayRepository.save(holiday);
        log.info("Created Holiday ID: {}", saved.getId());
        return ApiResponse.success("Holiday created successfully", saved);
    }

    @Transactional
    public ApiResponse<Holiday> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Holiday ID is required");
        }
        Holiday holiday = holidayRepository.findById(id).orElse(null);
        if (holiday == null) {
            return ApiResponse.error("Holiday not found with ID: " + id);
        }

        boolean currentStatus = Boolean.TRUE.equals(holiday.getIsActive());
        boolean newStatus = !currentStatus;
        holiday.setIsActive(newStatus);
        holiday.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Holiday updated = holidayRepository.save(holiday);
        log.info("Toggled active status for Holiday ID: {} to {}", id, newStatus);
        return ApiResponse.success("Holiday active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Holiday>> getHolidaysBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<Holiday> list;
        if (unitId != null && unitId > 0) {
            list = holidayRepository.findByUnitId(unitId);
        } else {
            list = holidayRepository.findAll();
        }
        return ApiResponse.success("Holidays retrieved successfully", list);
    }
}

