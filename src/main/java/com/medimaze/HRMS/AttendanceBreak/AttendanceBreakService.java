package com.medimaze.HRMS.AttendanceBreak;

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
public class AttendanceBreakService {

    private final AttendanceBreakRepository attendanceBreakRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<AttendanceBreak> saveOrUpdateAttendanceBreak(AttendanceBreak attendanceBreak) {
        if (attendanceBreak == null) {
            return ApiResponse.error("AttendanceBreak payload cannot be null");
        }

        if (attendanceBreak.getId() != null && attendanceBreak.getId() > 0) {
            AttendanceBreak existing = attendanceBreakRepository.findById(attendanceBreak.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("AttendanceBreak not found with ID: " + attendanceBreak.getId());
            }

            if (attendanceBreak.getBreakEndTime() != null) {
                existing.setBreakEndTime(attendanceBreak.getBreakEndTime());
            }
            if (attendanceBreak.getDurationMinutes() != null) {
                existing.setDurationMinutes(attendanceBreak.getDurationMinutes());
            }
            if (attendanceBreak.getRemarks() != null) {
                existing.setRemarks(attendanceBreak.getRemarks());
            }

            AttendanceBreak updated = attendanceBreakRepository.save(existing);
            return ApiResponse.success("AttendanceBreak updated successfully", updated);
        }

        if (attendanceBreak.getBreakStartTime() == null) {
            return ApiResponse.error("Break start time is required");
        }

        AttendanceBreak saved = attendanceBreakRepository.save(attendanceBreak);
        return ApiResponse.success("AttendanceBreak created successfully", saved);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<AttendanceBreak>> getAttendanceBreaksBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<AttendanceBreak> list = attendanceBreakRepository.findAll();
        return ApiResponse.success("Attendance breaks retrieved successfully", list);
    }
}
