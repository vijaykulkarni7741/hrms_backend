package com.webicSoftIndia.HRMS.Calendar;

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
public class CalendarService {

    private final CalendarRepository calendarRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Calendar> saveOrUpdateCalendar(Calendar calendar) {
        if (calendar == null) {
            return ApiResponse.error("Calendar payload cannot be null");
        }

        // UPDATE MODE
        if (calendar.getId() != null && calendar.getId() > 0) {
            Calendar existing = calendarRepository.findById(calendar.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Calendar not found with ID: " + calendar.getId());
            }

            if (calendar.getCalendarName() != null && !calendar.getCalendarName().trim().isEmpty()) {
                existing.setCalendarName(calendar.getCalendarName().trim());
            }
            if (calendar.getFinancialYear() != null) {
                existing.setFinancialYear(calendar.getFinancialYear());
            }
            if (calendar.getWeekStartDay() != null) {
                existing.setWeekStartDay(calendar.getWeekStartDay());
            }
            if (calendar.getWorkingDays() != null) {
                existing.setWorkingDays(calendar.getWorkingDays());
            }
            if (calendar.getHolidayPolicy() != null) {
                existing.setHolidayPolicy(calendar.getHolidayPolicy());
            }
            if (calendar.getLeavePolicy() != null) {
                existing.setLeavePolicy(calendar.getLeavePolicy());
            }
            if (calendar.getAttendancePolicy() != null) {
                existing.setAttendancePolicy(calendar.getAttendancePolicy());
            }
            if (calendar.getStatus() != null) {
                existing.setStatus(calendar.getStatus());
            }
            if (calendar.getIsActive() != null) {
                existing.setIsActive(calendar.getIsActive());
            }

            Calendar updated = calendarRepository.save(existing);
            log.info("Updated Calendar ID: {}", updated.getId());
            return ApiResponse.success("Calendar updated successfully", updated);
        }

        // CREATE MODE
        if (calendar.getCalendarCode() == null || calendar.getCalendarCode().trim().isEmpty()) {
            return ApiResponse.error("Calendar code is required");
        }
        if (calendar.getCalendarName() == null || calendar.getCalendarName().trim().isEmpty()) {
            return ApiResponse.error("Calendar name is required");
        }

        if (calendarRepository.findByCalendarCode(calendar.getCalendarCode().trim()).isPresent()) {
            return ApiResponse.error("Calendar code already exists");
        }

        if (calendar.getStatus() == null) {
            calendar.setStatus("ACTIVE");
        }
        if (calendar.getIsActive() == null) {
            calendar.setIsActive(true);
        }

        Calendar saved = calendarRepository.save(calendar);
        log.info("Created Calendar ID: {}", saved.getId());
        return ApiResponse.success("Calendar created successfully", saved);
    }

    @Transactional
    public ApiResponse<Calendar> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Calendar ID is required");
        }
        Calendar calendar = calendarRepository.findById(id).orElse(null);
        if (calendar == null) {
            return ApiResponse.error("Calendar not found with ID: " + id);
        }

        boolean currentStatus = Boolean.TRUE.equals(calendar.getIsActive());
        boolean newStatus = !currentStatus;
        calendar.setIsActive(newStatus);
        calendar.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Calendar updated = calendarRepository.save(calendar);
        log.info("Toggled active status for Calendar ID: {} to {}", id, newStatus);
        return ApiResponse.success("Calendar active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Calendar>> getCalendarsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<Calendar> list;
        if (unitId != null && unitId > 0) {
            list = calendarRepository.findByUnitId(unitId);
        } else {
            list = calendarRepository.findAll();
        }
        return ApiResponse.success("Calendars retrieved successfully", list);
    }
}

