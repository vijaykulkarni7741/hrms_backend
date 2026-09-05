package com.webicSoftIndia.HRMS.LeaveApproval;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.common.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-approvals")
@RequiredArgsConstructor
public class LeaveApprovalController {

    private final LeaveApprovalService leaveApprovalService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<LeaveApproval>> createOrUpdateLeaveApproval(@RequestBody LeaveApproval approval) {
        ApiResponse<LeaveApproval> response = leaveApprovalService.saveOrUpdateLeaveApproval(approval);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/toggle-active")
    public ResponseEntity<ApiResponse<LeaveApproval>> toggleActiveStatus(@RequestBody IdRequest request) {
        Long id = request != null ? request.getId() : null;
        ApiResponse<LeaveApproval> response = leaveApprovalService.toggleActiveStatus(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<LeaveApproval>>> listLeaveApprovals(
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

        ApiResponse<List<LeaveApproval>> response = leaveApprovalService.getLeaveApprovalsBySessionUnit(targetSessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
