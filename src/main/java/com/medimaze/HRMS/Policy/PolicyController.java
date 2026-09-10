package com.medimaze.HRMS.Policy;

import com.medimaze.HRMS.common.ApiResponse;
import com.medimaze.HRMS.common.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/policies", "/v1/policies", "/api/v1/admin/policies", "/v1/admin/policies", "/admin/policies"})
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @PostMapping({"/create", "/update"})
    public ResponseEntity<ApiResponse<Policy>> createOrUpdatePolicy(@RequestBody Policy policy) {
        ApiResponse<Policy> response = policyService.saveOrUpdatePolicy(policy);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/submit-approval")
    public ResponseEntity<ApiResponse<Policy>> submitForApproval(@RequestBody Map<String, Object> req) {
        Long id = req != null && req.get("id") != null ? Long.valueOf(req.get("id").toString()) : null;
        String username = req != null && req.get("username") != null ? req.get("username").toString() : "HR Executive";
        return ResponseEntity.ok(policyService.submitForApproval(id, username));
    }

    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<Policy>> approvePolicy(@RequestBody Map<String, Object> req) {
        Long id = req != null && req.get("id") != null ? Long.valueOf(req.get("id").toString()) : null;
        String approvedBy = req != null && req.get("approvedBy") != null ? req.get("approvedBy").toString() : "HR Manager";
        return ResponseEntity.ok(policyService.approvePolicy(id, approvedBy));
    }

    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<Policy>> rejectPolicy(@RequestBody Map<String, Object> req) {
        Long id = req != null && req.get("id") != null ? Long.valueOf(req.get("id").toString()) : null;
        String rejectedBy = req != null && req.get("rejectedBy") != null ? req.get("rejectedBy").toString() : "HR Manager";
        String reason = req != null && req.get("reason") != null ? req.get("reason").toString() : "Needs policy revision";
        return ResponseEntity.ok(policyService.rejectPolicy(id, rejectedBy, reason));
    }

    @PostMapping("/toggle-active")
    public ResponseEntity<ApiResponse<Policy>> toggleActiveStatus(@RequestBody IdRequest request) {
        Long id = request != null ? request.getId() : null;
        ApiResponse<Policy> response = policyService.toggleActiveStatus(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<Policy>>> listPolicies(
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

        ApiResponse<List<Policy>> response = policyService.getPoliciesBySessionUnit(targetSessionId);
        if (response.isSuccess() && response.getData() != null && !response.getData().isEmpty()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(policyService.getAllPolicies());
    }
}

