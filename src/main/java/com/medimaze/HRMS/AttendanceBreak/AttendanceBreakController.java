package com.medimaze.HRMS.AttendanceBreak;

import com.medimaze.HRMS.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance-breaks")
@RequiredArgsConstructor
public class AttendanceBreakController {

    private final AttendanceBreakService attendanceBreakService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AttendanceBreak>> createOrUpdateAttendanceBreak(@RequestBody AttendanceBreak attendanceBreak) {
        ApiResponse<AttendanceBreak> response = attendanceBreakService.saveOrUpdateAttendanceBreak(attendanceBreak);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<AttendanceBreak>>> listAttendanceBreaks(
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

        ApiResponse<List<AttendanceBreak>> response = attendanceBreakService.getAttendanceBreaksBySessionUnit(targetSessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
