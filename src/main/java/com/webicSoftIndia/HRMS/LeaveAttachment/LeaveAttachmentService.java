package com.webicSoftIndia.HRMS.LeaveAttachment;

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
public class LeaveAttachmentService {

    private final LeaveAttachmentRepository leaveAttachmentRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<LeaveAttachment> saveOrUpdateLeaveAttachment(LeaveAttachment attachment) {
        if (attachment == null) {
            return ApiResponse.error("LeaveAttachment payload cannot be null");
        }

        if (attachment.getId() != null && attachment.getId() > 0) {
            LeaveAttachment existing = leaveAttachmentRepository.findById(attachment.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("LeaveAttachment not found with ID: " + attachment.getId());
            }

            if (attachment.getFileName() != null) existing.setFileName(attachment.getFileName());
            if (attachment.getFilePath() != null) existing.setFilePath(attachment.getFilePath());
            if (attachment.getFileType() != null) existing.setFileType(attachment.getFileType());
            if (attachment.getFileSize() != null) existing.setFileSize(attachment.getFileSize());
            if (attachment.getStatus() != null) existing.setStatus(attachment.getStatus());
            if (attachment.getIsActive() != null) existing.setIsActive(attachment.getIsActive());

            LeaveAttachment updated = leaveAttachmentRepository.save(existing);
            return ApiResponse.success("LeaveAttachment updated successfully", updated);
        }

        if (attachment.getFileName() == null || attachment.getFileName().trim().isEmpty()) {
            return ApiResponse.error("File name is required");
        }

        if (attachment.getStatus() == null) attachment.setStatus("ACTIVE");
        if (attachment.getIsActive() == null) attachment.setIsActive(true);

        LeaveAttachment saved = leaveAttachmentRepository.save(attachment);
        return ApiResponse.success("LeaveAttachment created successfully", saved);
    }

    @Transactional
    public ApiResponse<LeaveAttachment> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid LeaveAttachment ID is required");
        }
        LeaveAttachment attachment = leaveAttachmentRepository.findById(id).orElse(null);
        if (attachment == null) {
            return ApiResponse.error("LeaveAttachment not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(attachment.getIsActive());
        attachment.setIsActive(newStatus);
        attachment.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        LeaveAttachment updated = leaveAttachmentRepository.save(attachment);
        return ApiResponse.success("LeaveAttachment active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<LeaveAttachment>> getLeaveAttachmentsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<LeaveAttachment> list;
        if (unitId != null && unitId > 0) {
            list = leaveAttachmentRepository.findByUnitId(unitId);
        } else {
            list = leaveAttachmentRepository.findAll();
        }
        return ApiResponse.success("LeaveAttachments retrieved successfully", list);
    }
}
