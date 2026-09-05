package com.webicSoftIndia.HRMS.Attendance;

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
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Attendance> saveOrUpdateAttendance(Attendance attendance) {
        if (attendance == null) {
            return ApiResponse.error("Attendance payload cannot be null");
        }

        if (attendance.getId() != null && attendance.getId() > 0) {
            Attendance existing = attendanceRepository.findById(attendance.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Attendance record not found with ID: " + attendance.getId());
            }

            if (attendance.getInTime() != null) {
                existing.setInTime(attendance.getInTime());
            }
            if (attendance.getOutTime() != null) {
                existing.setOutTime(attendance.getOutTime());
            }
            if (attendance.getTotalHours() != null) {
                existing.setTotalHours(attendance.getTotalHours());
            }
            if (attendance.getStatus() != null) {
                existing.setStatus(attendance.getStatus());
            }
            if (attendance.getIsLate() != null) {
                existing.setIsLate(attendance.getIsLate());
            }
            if (attendance.getIsEarlyDeparture() != null) {
                existing.setIsEarlyDeparture(attendance.getIsEarlyDeparture());
            }
            if (attendance.getOvertimeHours() != null) {
                existing.setOvertimeHours(attendance.getOvertimeHours());
            }
            if (attendance.getRemarks() != null) {
                existing.setRemarks(attendance.getRemarks());
            }
            if (attendance.getIsActive() != null) {
                existing.setIsActive(attendance.getIsActive());
            }

            Attendance updated = attendanceRepository.save(existing);
            return ApiResponse.success("Attendance updated successfully", updated);
        }

        if (attendance.getAttendanceDate() == null) {
            return ApiResponse.error("Attendance date is required");
        }
        if (attendance.getStatus() == null) {
            attendance.setStatus("PRESENT");
        }
        if (attendance.getIsActive() == null) {
            attendance.setIsActive(true);
        }

        Attendance saved = attendanceRepository.save(attendance);
        return ApiResponse.success("Attendance created successfully", saved);
    }

    @Transactional
    public ApiResponse<Attendance> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Attendance ID is required");
        }
        Attendance attendance = attendanceRepository.findById(id).orElse(null);
        if (attendance == null) {
            return ApiResponse.error("Attendance record not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(attendance.getIsActive());
        attendance.setIsActive(newStatus);
        attendance.setStatus(newStatus ? "PRESENT" : "INACTIVE");

        Attendance updated = attendanceRepository.save(attendance);
        return ApiResponse.success("Attendance active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Attendance>> getAttendanceBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<Attendance> list;
        if (unitId != null && unitId > 0) {
            list = attendanceRepository.findByUnitId(unitId);
        } else {
            list = attendanceRepository.findAll();
        }
        return ApiResponse.success("Attendance records retrieved successfully", list);
    }
}
