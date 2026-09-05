package com.webicSoftIndia.HRMS.Attendance;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.common.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

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

        String targetSessionId = null;
        if (xSessionId != null && !xSessionId.trim().isEmpty()) {
            targetSessionId = xSessionId.trim();
        } else if (sessionIdHeader != null && !sessionIdHeader.trim().isEmpty()) {
            targetSessionId = sessionIdHeader.trim();
        } else if (sessionIdParam != null && !sessionIdParam.trim().isEmpty()) {
            targetSessionId = sessionIdParam.trim();
        }

        ApiResponse<List<Attendance>> response = attendanceService.getAttendanceBySessionUnit(targetSessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
