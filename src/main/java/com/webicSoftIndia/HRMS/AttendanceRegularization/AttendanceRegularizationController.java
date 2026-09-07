package com.webicSoftIndia.HRMS.AttendanceRegularization;

import com.webicSoftIndia.HRMS.AttendanceRegularization.dto.RegularizationApprovalRequest;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance-regularizations")
@RequiredArgsConstructor
public class AttendanceRegularizationController {

    private final AttendanceRegularizationService regularizationService;

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

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AttendanceRegularization>> createOrUpdateRegularization(@RequestBody AttendanceRegularization regularization) {
        ApiResponse<AttendanceRegularization> response = regularizationService.saveOrUpdateRegularization(regularization);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping({"/approve", "/action"})
    public ResponseEntity<ApiResponse<AttendanceRegularization>> approveRegularization(
            @RequestBody RegularizationApprovalRequest request,
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {

        String sessionId = extractSessionId(xSessionId, sessionIdHeader, sessionIdParam);
        if (request != null && (request.getStatus() == null || request.getStatus().trim().isEmpty())) {
            request.setStatus("APPROVED");
        }
        ApiResponse<AttendanceRegularization> response = regularizationService.processRegularizationApproval(request, sessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<AttendanceRegularization>> rejectRegularization(
            @RequestBody RegularizationApprovalRequest request,
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {

        String sessionId = extractSessionId(xSessionId, sessionIdHeader, sessionIdParam);
        if (request != null) {
            request.setStatus("REJECTED");
        }
        ApiResponse<AttendanceRegularization> response = regularizationService.processRegularizationApproval(request, sessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<AttendanceRegularization>>> listRegularizations(
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {

        String targetSessionId = extractSessionId(xSessionId, sessionIdHeader, sessionIdParam);
        ApiResponse<List<AttendanceRegularization>> response = regularizationService.getRegularizationsBySessionUnit(targetSessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
