package com.medimaze.HRMS.AttendancePhoto;

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
public class AttendancePhotoService {

    private final AttendancePhotoRepository photoRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<AttendancePhoto> saveOrUpdateAttendancePhoto(AttendancePhoto photo) {
        if (photo == null) {
            return ApiResponse.error("AttendancePhoto payload cannot be null");
        }

        if (photo.getId() != null && photo.getId() > 0) {
            AttendancePhoto existing = photoRepository.findById(photo.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("AttendancePhoto not found with ID: " + photo.getId());
            }

            if (photo.getPhotoUrl() != null) {
                existing.setPhotoUrl(photo.getPhotoUrl());
            }
            if (photo.getVerificationScore() != null) {
                existing.setVerificationScore(photo.getVerificationScore());
            }
            if (photo.getStatus() != null) {
                existing.setStatus(photo.getStatus());
            }

            AttendancePhoto updated = photoRepository.save(existing);
            return ApiResponse.success("AttendancePhoto updated successfully", updated);
        }

        if (photo.getPhotoUrl() == null || photo.getPhotoUrl().trim().isEmpty()) {
            return ApiResponse.error("Photo URL is required");
        }
        if (photo.getStatus() == null) {
            photo.setStatus("VERIFIED");
        }

        AttendancePhoto saved = photoRepository.save(photo);
        return ApiResponse.success("AttendancePhoto created successfully", saved);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<AttendancePhoto>> getAttendancePhotosBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<AttendancePhoto> list = photoRepository.findAll();
        return ApiResponse.success("Attendance photos retrieved successfully", list);
    }
}
