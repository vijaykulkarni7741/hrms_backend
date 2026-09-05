package com.webicSoftIndia.HRMS.LeavePolicy;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.common.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-policies")
@RequiredArgsConstructor
public class LeavePolicyController {

    private final LeavePolicyService leavePolicyService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<LeavePolicy>> createOrUpdateLeavePolicy(@RequestBody LeavePolicy policy) {
        ApiResponse<LeavePolicy> response = leavePolicyService.saveOrUpdateLeavePolicy(policy);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/toggle-active")
    public ResponseEntity<ApiResponse<LeavePolicy>> toggleActiveStatus(@RequestBody IdRequest request) {
        Long id = request != null ? request.getId() : null;
        ApiResponse<LeavePolicy> response = leavePolicyService.toggleActiveStatus(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<LeavePolicy>>> listLeavePolicies(
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

        ApiResponse<List<LeavePolicy>> response = leavePolicyService.getLeavePoliciesBySessionUnit(targetSessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
