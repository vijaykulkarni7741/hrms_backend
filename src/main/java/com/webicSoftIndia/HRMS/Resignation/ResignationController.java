package com.webicSoftIndia.HRMS.Resignation;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.common.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({
        "/api/v1/resignations",
        "/v1/resignations",
        "/resignations",
        "/resignation"
})
@RequiredArgsConstructor
public class ResignationController {

    private final ResignationService resignationService;

    @PostMapping({"/submit", "/create", "/save"})
    public ResponseEntity<ApiResponse<Resignation>> submitResignation(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader) {

        String sessionId = xSessionId != null ? xSessionId : sessionIdHeader;
        ApiResponse<Resignation> response = resignationService.submitResignationFromMap(payload, sessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping({"/approve-reject", "/approve", "/process"})
    public ResponseEntity<ApiResponse<Resignation>> approveOrRejectResignation(@RequestBody Map<String, Object> payload) {
        ApiResponse<Resignation> response = resignationService.approveOrRejectResignation(payload);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/toggle-active")
    public ResponseEntity<ApiResponse<Resignation>> toggleActiveStatus(@RequestBody IdRequest request) {
        Long id = request != null ? request.getId() : null;
        ApiResponse<Resignation> response = resignationService.toggleActiveStatus(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping({"/list", "/queue", "/approval-queue", "/all"})
    public ResponseEntity<ApiResponse<List<Resignation>>> listResignations(
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader) {

        String targetSessionId = xSessionId != null ? xSessionId : sessionIdHeader;
        ApiResponse<List<Resignation>> response = resignationService.getResignationsBySessionUnit(targetSessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping({"/my-resignations", "/my", "/employee-resignations"})
    public ResponseEntity<ApiResponse<List<Resignation>>> getMyResignations(
            @RequestBody(required = false) Map<String, Object> payload,
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader) {

        Long empId = null;
        if (payload != null && payload.get("employeeId") != null) {
            try {
                empId = Long.valueOf(payload.get("employeeId").toString());
            } catch (Exception ignored) {}
        }

        String targetSessionId = xSessionId != null ? xSessionId : sessionIdHeader;
        ApiResponse<List<Resignation>> response = resignationService.getResignationsByEmployee(empId, targetSessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
