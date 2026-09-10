package com.medimaze.HRMS.SalarySlip;

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
public class SalarySlipService {

    private final SalarySlipRepository salarySlipRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<SalarySlip> saveOrUpdateSalarySlip(SalarySlip slip) {
        if (slip == null) {
            return ApiResponse.error("SalarySlip payload cannot be null");
        }

        if (slip.getId() != null && slip.getId() > 0) {
            SalarySlip existing = salarySlipRepository.findById(slip.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("SalarySlip not found with ID: " + slip.getId());
            }

            if (slip.getSlipNumber() != null) existing.setSlipNumber(slip.getSlipNumber());
            if (slip.getNetSalary() != null) existing.setNetSalary(slip.getNetSalary());
            if (slip.getPdfUrl() != null) existing.setPdfUrl(slip.getPdfUrl());
            if (slip.getStatus() != null) existing.setStatus(slip.getStatus());
            if (slip.getIsActive() != null) existing.setIsActive(slip.getIsActive());

            SalarySlip updated = salarySlipRepository.save(existing);
            return ApiResponse.success("SalarySlip updated successfully", updated);
        }

        if (slip.getSlipNumber() == null || slip.getSlipNumber().trim().isEmpty()) {
            return ApiResponse.error("Slip number is required");
        }

        if (slip.getStatus() == null) slip.setStatus("GENERATED");
        if (slip.getIsActive() == null) slip.setIsActive(true);

        SalarySlip saved = salarySlipRepository.save(slip);
        return ApiResponse.success("SalarySlip created successfully", saved);
    }

    @Transactional
    public ApiResponse<SalarySlip> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid SalarySlip ID is required");
        }
        SalarySlip slip = salarySlipRepository.findById(id).orElse(null);
        if (slip == null) {
            return ApiResponse.error("SalarySlip not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(slip.getIsActive());
        slip.setIsActive(newStatus);
        slip.setStatus(newStatus ? "GENERATED" : "CANCELLED");

        SalarySlip updated = salarySlipRepository.save(slip);
        return ApiResponse.success("SalarySlip active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<SalarySlip>> getSalarySlipsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<SalarySlip> list;
        if (unitId != null && unitId > 0) {
            list = salarySlipRepository.findByUnitId(unitId);
        } else {
            list = salarySlipRepository.findAll();
        }
        return ApiResponse.success("SalarySlips retrieved successfully", list);
    }
}
