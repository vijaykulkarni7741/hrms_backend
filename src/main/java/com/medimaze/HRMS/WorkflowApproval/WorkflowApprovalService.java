package com.medimaze.HRMS.WorkflowApproval;

import com.medimaze.HRMS.Employee.dto.EmployeeDto;
import com.medimaze.HRMS.common.ApiResponse;
import com.medimaze.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowApprovalService {

    private final WorkflowApprovalRepository workflowApprovalRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<WorkflowApproval> saveOrUpdateWorkflowApproval(WorkflowApproval approval) {
        if (approval == null) {
            return ApiResponse.error("WorkflowApproval payload cannot be null");
        }

        if (approval.getId() != null && approval.getId() > 0) {
            WorkflowApproval existing = workflowApprovalRepository.findById(approval.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("WorkflowApproval not found with ID: " + approval.getId());
            }

            if (approval.getAction() != null) existing.setAction(approval.getAction());
            if (approval.getComments() != null) existing.setComments(approval.getComments());
            if (approval.getStatus() != null) existing.setStatus(approval.getStatus());
            if (approval.getIsActive() != null) existing.setIsActive(approval.getIsActive());

            WorkflowApproval updated = workflowApprovalRepository.save(existing);
            return ApiResponse.success("WorkflowApproval updated successfully", updated);
        }

        if (approval.getStatus() == null) approval.setStatus("APPROVED");
        if (approval.getIsActive() == null) approval.setIsActive(true);

        WorkflowApproval saved = workflowApprovalRepository.save(approval);
        return ApiResponse.success("WorkflowApproval created successfully", saved);
    }

    @Transactional
    public ApiResponse<WorkflowApproval> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid WorkflowApproval ID is required");
        }
        WorkflowApproval approval = workflowApprovalRepository.findById(id).orElse(null);
        if (approval == null) {
            return ApiResponse.error("WorkflowApproval not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(approval.getIsActive());
        approval.setIsActive(newStatus);
        approval.setStatus(newStatus ? "APPROVED" : "REJECTED");

        WorkflowApproval updated = workflowApprovalRepository.save(approval);
        return ApiResponse.success("WorkflowApproval active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<WorkflowApproval>> getWorkflowApprovalsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<WorkflowApproval> list;
        if (unitId != null && unitId > 0) {
            list = workflowApprovalRepository.findByUnitId(unitId);
        } else {
            list = workflowApprovalRepository.findAll();
        }
        return ApiResponse.success("WorkflowApprovals retrieved successfully", list);
    }
}
