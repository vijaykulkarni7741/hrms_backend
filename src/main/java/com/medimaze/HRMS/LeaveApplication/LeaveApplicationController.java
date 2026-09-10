package com.medimaze.HRMS.LeaveApplication;

import com.medimaze.HRMS.common.ApiResponse;
import com.medimaze.HRMS.common.IdRequest;
import com.medimaze.HRMS.common.PageRequestDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/leave-applications", "/v1/leave-applications", "/api/v1/leave/applications", "/v1/leave/applications", "/api/v1/leave", "/v1/leave"})
@RequiredArgsConstructor
public class LeaveApplicationController {

    private final LeaveApplicationService leaveApplicationService;

    @Data
    public static class ManagerApprovalRequest {
        private Long applicationId;
        private Long id; // alias for applicationId
        private Long managerEmployeeId;
        private String action; // APPROVED / REJECTED
        private String status; // alias for action
    }

    @PostMapping({"/create", "/apply"})
    public ResponseEntity<ApiResponse<LeaveApplication>> createOrUpdateLeaveApplication(@RequestBody LeaveApplication application) {
        ApiResponse<LeaveApplication> response = leaveApplicationService.saveOrUpdateLeaveApplication(application);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping({"/approve", "/process-approval"})
    public ResponseEntity<ApiResponse<LeaveApplication>> approveLeaveApplication(@RequestBody ManagerApprovalRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Approval payload is required"));
        }
        Long appId = request.getApplicationId() != null ? request.getApplicationId() : request.getId();
        String act = request.getAction() != null ? request.getAction() : request.getStatus();

        ApiResponse<LeaveApplication> response = leaveApplicationService.processManagerApproval(appId, request.getManagerEmployeeId(), act);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/toggle-active")
    public ResponseEntity<ApiResponse<LeaveApplication>> toggleActiveStatus(@RequestBody IdRequest request) {
        Long id = request != null ? request.getId() : null;
        ApiResponse<LeaveApplication> response = leaveApplicationService.toggleActiveStatus(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping({"/list", "/my", "/approvals"})
    public ResponseEntity<ApiResponse<List<LeaveApplication>>> listLeaveApplications(
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam,
            @RequestBody(required = false) PageRequestDto pageRequest) {

        String targetSessionId = null;
        if (xSessionId != null && !xSessionId.trim().isEmpty()) {
            targetSessionId = xSessionId.trim();
        } else if (sessionIdHeader != null && !sessionIdHeader.trim().isEmpty()) {
            targetSessionId = sessionIdHeader.trim();
        } else if (sessionIdParam != null && !sessionIdParam.trim().isEmpty()) {
            targetSessionId = sessionIdParam.trim();
        }

        ApiResponse<List<LeaveApplication>> response = leaveApplicationService.getLeaveApplicationsBySessionUnit(targetSessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
