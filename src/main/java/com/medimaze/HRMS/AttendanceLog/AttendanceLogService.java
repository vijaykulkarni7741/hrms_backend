package com.medimaze.HRMS.AttendanceLog;

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
public class AttendanceLogService {

    private final AttendanceLogRepository attendanceLogRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<AttendanceLog> saveOrUpdateAttendanceLog(AttendanceLog attendanceLog) {
        if (attendanceLog == null) {
            return ApiResponse.error("AttendanceLog payload cannot be null");
        }

        if (attendanceLog.getId() != null && attendanceLog.getId() > 0) {
            AttendanceLog existing = attendanceLogRepository.findById(attendanceLog.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("AttendanceLog not found with ID: " + attendanceLog.getId());
            }

            if (attendanceLog.getPunchTime() != null) {
                existing.setPunchTime(attendanceLog.getPunchTime());
            }
            if (attendanceLog.getPunchType() != null) {
                existing.setPunchType(attendanceLog.getPunchType());
            }
            if (attendanceLog.getLocationName() != null) {
                existing.setLocationName(attendanceLog.getLocationName());
            }

            AttendanceLog updated = attendanceLogRepository.save(existing);
            return ApiResponse.success("AttendanceLog updated successfully", updated);
        }

        if (attendanceLog.getPunchTime() == null) {
            return ApiResponse.error("Punch time is required");
        }
        if (attendanceLog.getPunchType() == null) {
            attendanceLog.setPunchType("IN");
        }

        AttendanceLog saved = attendanceLogRepository.save(attendanceLog);
        return ApiResponse.success("AttendanceLog created successfully", saved);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<AttendanceLog>> getAttendanceLogsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<AttendanceLog> list = attendanceLogRepository.findAll();
        return ApiResponse.success("Attendance logs retrieved successfully", list);
    }
}
