package com.webicSoftIndia.HRMS.WorkflowHistory;

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
public class WorkflowHistoryService {

    private final WorkflowHistoryRepository workflowHistoryRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<WorkflowHistory> saveOrUpdateWorkflowHistory(WorkflowHistory history) {
        if (history == null) {
            return ApiResponse.error("WorkflowHistory payload cannot be null");
        }

        if (history.getId() != null && history.getId() > 0) {
            WorkflowHistory existing = workflowHistoryRepository.findById(history.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("WorkflowHistory not found with ID: " + history.getId());
            }

            if (history.getActionTaken() != null) existing.setActionTaken(history.getActionTaken());
            if (history.getPerformedBy() != null) existing.setPerformedBy(history.getPerformedBy());
            if (history.getRemarks() != null) existing.setRemarks(history.getRemarks());
            if (history.getStatus() != null) existing.setStatus(history.getStatus());
            if (history.getIsActive() != null) existing.setIsActive(history.getIsActive());

            WorkflowHistory updated = workflowHistoryRepository.save(existing);
            return ApiResponse.success("WorkflowHistory updated successfully", updated);
        }

        if (history.getActionTaken() == null || history.getActionTaken().trim().isEmpty()) {
            return ApiResponse.error("Action taken is required");
        }

        if (history.getStatus() == null) history.setStatus("LOGGED");
        if (history.getIsActive() == null) history.setIsActive(true);

        WorkflowHistory saved = workflowHistoryRepository.save(history);
        return ApiResponse.success("WorkflowHistory created successfully", saved);
    }

    @Transactional
    public ApiResponse<WorkflowHistory> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid WorkflowHistory ID is required");
        }
        WorkflowHistory history = workflowHistoryRepository.findById(id).orElse(null);
        if (history == null) {
            return ApiResponse.error("WorkflowHistory not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(history.getIsActive());
        history.setIsActive(newStatus);
        history.setStatus(newStatus ? "LOGGED" : "ARCHIVED");

        WorkflowHistory updated = workflowHistoryRepository.save(history);
        return ApiResponse.success("WorkflowHistory active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<WorkflowHistory>> getWorkflowHistoriesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<WorkflowHistory> list;
        if (unitId != null && unitId > 0) {
            list = workflowHistoryRepository.findByUnitId(unitId);
        } else {
            list = workflowHistoryRepository.findAll();
        }
        return ApiResponse.success("WorkflowHistories retrieved successfully", list);
    }
}
