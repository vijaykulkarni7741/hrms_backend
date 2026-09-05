package com.webicSoftIndia.HRMS.LeaveApproval;

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
public class LeaveApprovalService {

    private final LeaveApprovalRepository leaveApprovalRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<LeaveApproval> saveOrUpdateLeaveApproval(LeaveApproval approval) {
        if (approval == null) {
            return ApiResponse.error("LeaveApproval payload cannot be null");
        }

        if (approval.getId() != null && approval.getId() > 0) {
            LeaveApproval existing = leaveApprovalRepository.findById(approval.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("LeaveApproval not found with ID: " + approval.getId());
            }

            if (approval.getAction() != null) existing.setAction(approval.getAction());
            if (approval.getComments() != null) existing.setComments(approval.getComments());
            if (approval.getStatus() != null) existing.setStatus(approval.getStatus());
            if (approval.getIsActive() != null) existing.setIsActive(approval.getIsActive());

            LeaveApproval updated = leaveApprovalRepository.save(existing);
            return ApiResponse.success("LeaveApproval updated successfully", updated);
        }

        if (approval.getStatus() == null) approval.setStatus("APPROVED");
        if (approval.getIsActive() == null) approval.setIsActive(true);

        LeaveApproval saved = leaveApprovalRepository.save(approval);
        return ApiResponse.success("LeaveApproval created successfully", saved);
    }

    @Transactional
    public ApiResponse<LeaveApproval> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid LeaveApproval ID is required");
        }
        LeaveApproval approval = leaveApprovalRepository.findById(id).orElse(null);
        if (approval == null) {
            return ApiResponse.error("LeaveApproval not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(approval.getIsActive());
        approval.setIsActive(newStatus);
        approval.setStatus(newStatus ? "APPROVED" : "REJECTED");

        LeaveApproval updated = leaveApprovalRepository.save(approval);
        return ApiResponse.success("LeaveApproval active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<LeaveApproval>> getLeaveApprovalsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<LeaveApproval> list;
        if (unitId != null && unitId > 0) {
            list = leaveApprovalRepository.findByUnitId(unitId);
        } else {
            list = leaveApprovalRepository.findAll();
        }
        return ApiResponse.success("LeaveApprovals retrieved successfully", list);
    }
}
