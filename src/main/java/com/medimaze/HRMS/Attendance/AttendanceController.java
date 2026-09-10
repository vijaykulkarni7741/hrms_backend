package com.medimaze.HRMS.Attendance;

import com.medimaze.HRMS.Attendance.dto.MarkAttendanceRequest;
import com.medimaze.HRMS.Attendance.dto.RegularizeAttendanceRequest;
import com.medimaze.HRMS.common.ApiResponse;
import com.medimaze.HRMS.common.IdRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    private String extractSessionId(String xSessionId, String sessionIdHeader, String sessionIdParam) {
        if (xSessionId != null && !xSessionId.trim().isEmpty()) {
            return xSessionId.trim();
        } else if (sessionIdHeader != null && !sessionIdHeader.trim().isEmpty()) {
            return sessionIdHeader.trim();
        } else if (sessionIdParam != null && !sessionIdParam.trim().isEmpty()) {
            return sessionIdParam.trim();
        }
        return null;
    }

    @PostMapping({"/check-in", "/in"})
    public ResponseEntity<ApiResponse<Attendance>> markCheckIn(
            @RequestBody(required = false) MarkAttendanceRequest request,
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {
        
        String sessionId = extractSessionId(xSessionId, sessionIdHeader, sessionIdParam);
        ApiResponse<Attendance> response = attendanceService.markCheckIn(request, sessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping({"/check-out", "/out"})
    public ResponseEntity<ApiResponse<Attendance>> markCheckOut(
            @RequestBody(required = false) MarkAttendanceRequest request,
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {
        
        String sessionId = extractSessionId(xSessionId, sessionIdHeader, sessionIdParam);
        ApiResponse<Attendance> response = attendanceService.markCheckOut(request, sessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping({"/today", "/my-today"})
    public ResponseEntity<ApiResponse<Attendance>> getTodayAttendance(
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {
        
        String sessionId = extractSessionId(xSessionId, sessionIdHeader, sessionIdParam);
        ApiResponse<Attendance> response = attendanceService.getTodayAttendance(sessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyLogsRequest {
        private Integer month;
        private Integer year;
        private Integer days;
        private String fromDate;
        private String toDate;
    }

    @PostMapping({"/my-logs", "/monthly-logs"})
    public ResponseEntity<ApiResponse<List<Attendance>>> getMyMonthlyLogs(
            @RequestBody(required = false) MonthlyLogsRequest request,
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {

        Integer month = request != null ? request.getMonth() : null;
        Integer year = request != null ? request.getYear() : null;
        Integer days = request != null ? request.getDays() : null;
        String fromDate = request != null ? request.getFromDate() : null;
        String toDate = request != null ? request.getToDate() : null;

        String sessionId = extractSessionId(xSessionId, sessionIdHeader, sessionIdParam);
        ApiResponse<List<Attendance>> response = attendanceService.getEmployeeMonthlyAttendanceLogs(sessionId, month, year, days, fromDate, toDate);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping({"/regularize", "/regularize-punch"})
    public ResponseEntity<ApiResponse<Attendance>> regularizeAttendance(
            @RequestBody RegularizeAttendanceRequest request,
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {

        String sessionId = extractSessionId(xSessionId, sessionIdHeader, sessionIdParam);
        Long id = request != null ? request.getAttendanceId() : null;
        String dateStr = request != null ? request.getAttendanceDate() : null;
        String inTime = request != null ? request.getInTime() : null;
        String outTime = request != null ? request.getOutTime() : null;
        String reason = request != null ? request.getReason() : null;

        ApiResponse<Attendance> response = attendanceService.regularizeAttendance(id, dateStr, inTime, outTime, reason, sessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/apply-comp-off")
    public ResponseEntity<ApiResponse<Attendance>> applyCompOff(
            @RequestBody(required = false) java.util.Map<String, String> payload,
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {

        String sessionId = extractSessionId(xSessionId, sessionIdHeader, sessionIdParam);
        String dateStr = payload != null ? payload.get("attendanceDate") : null;
        String targetLeaveDateStr = payload != null ? (payload.get("targetLeaveDate") != null ? payload.get("targetLeaveDate") : payload.get("targetDate")) : null;
        String reason = payload != null ? payload.get("reason") : null;

        ApiResponse<Attendance> response = attendanceService.applyCompOff(dateStr, targetLeaveDateStr, reason, sessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Attendance>> createOrUpdateAttendance(@RequestBody Attendance attendance) {
        ApiResponse<Attendance> response = attendanceService.saveOrUpdateAttendance(attendance);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/toggle-active")
    public ResponseEntity<ApiResponse<Attendance>> toggleActiveStatus(@RequestBody IdRequest request) {
        Long id = request != null ? request.getId() : null;
        ApiResponse<Attendance> response = attendanceService.toggleActiveStatus(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<Attendance>>> listAttendance(
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {

        String targetSessionId = extractSessionId(xSessionId, sessionIdHeader, sessionIdParam);
        ApiResponse<List<Attendance>> response = attendanceService.getAttendanceBySessionUnit(targetSessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @RequestMapping(value = "/unconsumed-weekoffs", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<ApiResponse<List<Attendance>>> getUnconsumedWeekOffs(
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {

        String sessionId = extractSessionId(xSessionId, sessionIdHeader, sessionIdParam);
        ApiResponse<List<Attendance>> response = attendanceService.getUnconsumedWeekOffs(sessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
