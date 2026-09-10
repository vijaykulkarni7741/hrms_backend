package com.medimaze.HRMS.Grade;

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
public class GradeService {

    private final GradeRepository gradeRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Grade> saveOrUpdateGrade(Grade grade) {
        if (grade == null) {
            return ApiResponse.error("Grade payload cannot be null");
        }

        // UPDATE MODE
        if (grade.getId() != null && grade.getId() > 0) {
            Grade existing = gradeRepository.findById(grade.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Grade not found with ID: " + grade.getId());
            }

            if (grade.getGradeName() != null && !grade.getGradeName().trim().isEmpty()) {
                existing.setGradeName(grade.getGradeName().trim());
            }
            if (grade.getGradeLevel() != null) {
                existing.setGradeLevel(grade.getGradeLevel());
            }
            if (grade.getDescription() != null) {
                existing.setDescription(grade.getDescription());
            }
            if (grade.getStatus() != null) {
                existing.setStatus(grade.getStatus());
            }
            if (grade.getIsActive() != null) {
                existing.setIsActive(grade.getIsActive());
            }

            Grade updated = gradeRepository.save(existing);
            log.info("Updated Grade ID: {}", updated.getId());
            return ApiResponse.success("Grade updated successfully", updated);
        }

        // CREATE MODE
        if (grade.getGradeCode() == null || grade.getGradeCode().trim().isEmpty()) {
            return ApiResponse.error("Grade code is required");
        }
        if (grade.getGradeName() == null || grade.getGradeName().trim().isEmpty()) {
            return ApiResponse.error("Grade name is required");
        }

        if (gradeRepository.findByGradeCode(grade.getGradeCode().trim()).isPresent()) {
            return ApiResponse.error("Grade code already exists");
        }

        if (grade.getStatus() == null) {
            grade.setStatus("ACTIVE");
        }
        if (grade.getIsActive() == null) {
            grade.setIsActive(true);
        }

        Grade saved = gradeRepository.save(grade);
        log.info("Created Grade ID: {}", saved.getId());
        return ApiResponse.success("Grade created successfully", saved);
    }

    @Transactional
    public ApiResponse<Grade> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Grade ID is required");
        }
        Grade grade = gradeRepository.findById(id).orElse(null);
        if (grade == null) {
            return ApiResponse.error("Grade not found with ID: " + id);
        }

        boolean currentStatus = Boolean.TRUE.equals(grade.getIsActive());
        boolean newStatus = !currentStatus;
        grade.setIsActive(newStatus);
        grade.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Grade updated = gradeRepository.save(grade);
        log.info("Toggled active status for Grade ID: {} to {}", id, newStatus);
        return ApiResponse.success("Grade active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Grade>> getGradesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<Grade> list = gradeRepository.findAll();
        return ApiResponse.success("Grades retrieved successfully", list);
    }
}

