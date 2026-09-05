package com.webicSoftIndia.HRMS.AttendanceRegularization;

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
public class AttendanceRegularizationService {

    private final AttendanceRegularizationRepository regularizationRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<AttendanceRegularization> saveOrUpdateRegularization(AttendanceRegularization regularization) {
        if (regularization == null) {
            return ApiResponse.error("AttendanceRegularization payload cannot be null");
        }

        if (regularization.getId() != null && regularization.getId() > 0) {
            AttendanceRegularization existing = regularizationRepository.findById(regularization.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("AttendanceRegularization not found with ID: " + regularization.getId());
            }

            if (regularization.getStatus() != null) {
                existing.setStatus(regularization.getStatus());
            }
            if (regularization.getApprovalRemarks() != null) {
                existing.setApprovalRemarks(regularization.getApprovalRemarks());
            }

            AttendanceRegularization updated = regularizationRepository.save(existing);
            return ApiResponse.success("AttendanceRegularization updated successfully", updated);
        }

        if (regularization.getRegularizationDate() == null) {
            return ApiResponse.error("Regularization date is required");
        }
        if (regularization.getStatus() == null) {
            regularization.setStatus("PENDING");
        }

        AttendanceRegularization saved = regularizationRepository.save(regularization);
        return ApiResponse.success("AttendanceRegularization created successfully", saved);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<AttendanceRegularization>> getRegularizationsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<AttendanceRegularization> list = regularizationRepository.findAll();
        return ApiResponse.success("Attendance regularizations retrieved successfully", list);
    }
}
