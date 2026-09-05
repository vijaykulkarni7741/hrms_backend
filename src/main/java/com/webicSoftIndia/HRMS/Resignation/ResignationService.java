package com.webicSoftIndia.HRMS.Resignation;

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
public class ResignationService {

    private final ResignationRepository resignationRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Resignation> saveOrUpdateResignation(Resignation resignation) {
        if (resignation == null) {
            return ApiResponse.error("Resignation payload cannot be null");
        }

        if (resignation.getId() != null && resignation.getId() > 0) {
            Resignation existing = resignationRepository.findById(resignation.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Resignation record not found with ID: " + resignation.getId());
            }

            if (resignation.getResignationDate() != null) existing.setResignationDate(resignation.getResignationDate());
            if (resignation.getNoticePeriodDays() != null) existing.setNoticePeriodDays(resignation.getNoticePeriodDays());
            if (resignation.getLastWorkingDay() != null) existing.setLastWorkingDay(resignation.getLastWorkingDay());
            if (resignation.getReason() != null) existing.setReason(resignation.getReason());
            if (resignation.getStatus() != null) existing.setStatus(resignation.getStatus());
            if (resignation.getIsActive() != null) existing.setIsActive(resignation.getIsActive());

            Resignation updated = resignationRepository.save(existing);
            return ApiResponse.success("Resignation updated successfully", updated);
        }

        if (resignation.getResignationDate() == null) {
            return ApiResponse.error("Resignation date is required");
        }

        if (resignation.getStatus() == null) resignation.setStatus("PENDING");
        if (resignation.getIsActive() == null) resignation.setIsActive(true);

        Resignation saved = resignationRepository.save(resignation);
        return ApiResponse.success("Resignation created successfully", saved);
    }

    @Transactional
    public ApiResponse<Resignation> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Resignation ID is required");
        }
        Resignation resignation = resignationRepository.findById(id).orElse(null);
        if (resignation == null) {
            return ApiResponse.error("Resignation record not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(resignation.getIsActive());
        resignation.setIsActive(newStatus);
        resignation.setStatus(newStatus ? "PENDING" : "INACTIVE");

        Resignation updated = resignationRepository.save(resignation);
        return ApiResponse.success("Resignation active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Resignation>> getResignationsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<Resignation> list;
        if (unitId != null && unitId > 0) {
            list = resignationRepository.findByUnitId(unitId);
        } else {
            list = resignationRepository.findAll();
        }
        return ApiResponse.success("Resignations retrieved successfully", list);
    }
}
