package com.medimaze.HRMS.Roster;

import com.medimaze.HRMS.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/roster", "/v1/roster"})
public class EmployeeRosterController {

    private final EmployeeRosterService rosterService;

    public EmployeeRosterController(EmployeeRosterService rosterService) {
        this.rosterService = rosterService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<List<EmployeeRoster>>> createRoster(@RequestBody EmployeeRosterService.CreateRosterRequest request) {
        ApiResponse<List<EmployeeRoster>> response = rosterService.createRoster(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/employee-summary")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getHRRosterSummaryList() {
        ApiResponse<List<Map<String, Object>>> response = rosterService.getHRRosterSummaryList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<EmployeeRoster>>> getAllRosters() {
        ApiResponse<List<EmployeeRoster>> response = rosterService.getAllRosters();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteRoster(@RequestBody Map<String, Long> payload) {
        Long id = payload != null ? payload.get("id") : null;
        ApiResponse<String> response = rosterService.deleteRoster(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns the current employee's active shift and roster for today.
     * Uses session ID to resolve the employee automatically.
     */
    @PostMapping("/my-roster")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyRoster(
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {

        String sessionId = null;
        if (xSessionId != null && !xSessionId.trim().isEmpty()) sessionId = xSessionId.trim();
        else if (sessionIdHeader != null && !sessionIdHeader.trim().isEmpty()) sessionId = sessionIdHeader.trim();
        else if (sessionIdParam != null && !sessionIdParam.trim().isEmpty()) sessionId = sessionIdParam.trim();

        ApiResponse<Map<String, Object>> response = rosterService.getMyRoster(sessionId);
        return ResponseEntity.ok(response);
    }
}

