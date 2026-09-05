package com.webicSoftIndia.HRMS.Workflow;

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
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Workflow> saveOrUpdateWorkflow(Workflow workflow) {
        if (workflow == null) {
            return ApiResponse.error("Workflow payload cannot be null");
        }

        if (workflow.getId() != null && workflow.getId() > 0) {
            Workflow existing = workflowRepository.findById(workflow.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Workflow not found with ID: " + workflow.getId());
            }

            if (workflow.getWorkflowName() != null) existing.setWorkflowName(workflow.getWorkflowName());
            if (workflow.getModuleType() != null) existing.setModuleType(workflow.getModuleType());
            if (workflow.getDescription() != null) existing.setDescription(workflow.getDescription());
            if (workflow.getStatus() != null) existing.setStatus(workflow.getStatus());
            if (workflow.getIsActive() != null) existing.setIsActive(workflow.getIsActive());

            Workflow updated = workflowRepository.save(existing);
            return ApiResponse.success("Workflow updated successfully", updated);
        }

        if (workflow.getWorkflowName() == null || workflow.getWorkflowName().trim().isEmpty()) {
            return ApiResponse.error("Workflow name is required");
        }

        if (workflow.getStatus() == null) workflow.setStatus("ACTIVE");
        if (workflow.getIsActive() == null) workflow.setIsActive(true);

        Workflow saved = workflowRepository.save(workflow);
        return ApiResponse.success("Workflow created successfully", saved);
    }

    @Transactional
    public ApiResponse<Workflow> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Workflow ID is required");
        }
        Workflow workflow = workflowRepository.findById(id).orElse(null);
        if (workflow == null) {
            return ApiResponse.error("Workflow not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(workflow.getIsActive());
        workflow.setIsActive(newStatus);
        workflow.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Workflow updated = workflowRepository.save(workflow);
        return ApiResponse.success("Workflow active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Workflow>> getWorkflowsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<Workflow> list;
        if (unitId != null && unitId > 0) {
            list = workflowRepository.findByUnitId(unitId);
        } else {
            list = workflowRepository.findAll();
        }
        return ApiResponse.success("Workflows retrieved successfully", list);
    }
}
