package com.webicSoftIndia.HRMS.WorkflowStep;

import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowStepService {

    private final WorkflowStepRepository workflowStepRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<WorkflowStep> saveOrUpdateWorkflowStep(WorkflowStep step) {
        if (step == null) {
            return ApiResponse.error("WorkflowStep payload cannot be null");
        }

        if (step.getId() != null && step.getId() > 0) {
            WorkflowStep existing = workflowStepRepository.findById(step.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("WorkflowStep not found with ID: " + step.getId());
            }

            if (step.getStepOrder() != null) existing.setStepOrder(step.getStepOrder());
            if (step.getStepName() != null) existing.setStepName(step.getStepName());
            if (step.getApproverRole() != null) existing.setApproverRole(step.getApproverRole());
            if (step.getStatus() != null) existing.setStatus(step.getStatus());
            if (step.getIsActive() != null) existing.setIsActive(step.getIsActive());

            WorkflowStep updated = workflowStepRepository.save(existing);
            return ApiResponse.success("WorkflowStep updated successfully", updated);
        }

        if (step.getStepName() == null || step.getStepName().trim().isEmpty()) {
            return ApiResponse.error("Step name is required");
        }

        if (step.getStatus() == null) step.setStatus("ACTIVE");
        if (step.getIsActive() == null) step.setIsActive(true);

        WorkflowStep saved = workflowStepRepository.save(step);
        return ApiResponse.success("WorkflowStep created successfully", saved);
    }

    @Transactional
    public ApiResponse<WorkflowStep> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid WorkflowStep ID is required");
        }
        WorkflowStep step = workflowStepRepository.findById(id).orElse(null);
        if (step == null) {
            return ApiResponse.error("WorkflowStep not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(step.getIsActive());
        step.setIsActive(newStatus);
        step.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        WorkflowStep updated = workflowStepRepository.save(step);
        return ApiResponse.success("WorkflowStep active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<WorkflowStep>> getWorkflowStepsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<WorkflowStep> list;
        if (unitId != null && unitId > 0) {
            list = workflowStepRepository.findByUnitId(unitId);
        } else {
            list = workflowStepRepository.findAll();
        }
        return ApiResponse.success("WorkflowSteps retrieved successfully", list);
    }
}
