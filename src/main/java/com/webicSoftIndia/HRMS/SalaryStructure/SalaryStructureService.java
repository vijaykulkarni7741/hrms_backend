package com.webicSoftIndia.HRMS.SalaryStructure;

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
public class SalaryStructureService {

    private final SalaryStructureRepository salaryStructureRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<SalaryStructure> saveOrUpdateSalaryStructure(SalaryStructure structure) {
        if (structure == null) {
            return ApiResponse.error("SalaryStructure payload cannot be null");
        }

        if (structure.getId() != null && structure.getId() > 0) {
            SalaryStructure existing = salaryStructureRepository.findById(structure.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("SalaryStructure not found with ID: " + structure.getId());
            }

            if (structure.getStructureName() != null) existing.setStructureName(structure.getStructureName());
            if (structure.getDescription() != null) existing.setDescription(structure.getDescription());
            if (structure.getBaseCtc() != null) existing.setBaseCtc(structure.getBaseCtc());
            if (structure.getStatus() != null) existing.setStatus(structure.getStatus());
            if (structure.getIsActive() != null) existing.setIsActive(structure.getIsActive());

            SalaryStructure updated = salaryStructureRepository.save(existing);
            return ApiResponse.success("SalaryStructure updated successfully", updated);
        }

        if (structure.getStructureName() == null || structure.getStructureName().trim().isEmpty()) {
            return ApiResponse.error("Structure name is required");
        }

        if (structure.getStatus() == null) structure.setStatus("ACTIVE");
        if (structure.getIsActive() == null) structure.setIsActive(true);

        SalaryStructure saved = salaryStructureRepository.save(structure);
        return ApiResponse.success("SalaryStructure created successfully", saved);
    }

    @Transactional
    public ApiResponse<SalaryStructure> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid SalaryStructure ID is required");
        }
        SalaryStructure structure = salaryStructureRepository.findById(id).orElse(null);
        if (structure == null) {
            return ApiResponse.error("SalaryStructure not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(structure.getIsActive());
        structure.setIsActive(newStatus);
        structure.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        SalaryStructure updated = salaryStructureRepository.save(structure);
        return ApiResponse.success("SalaryStructure active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<SalaryStructure>> getSalaryStructuresBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<SalaryStructure> list;
        if (unitId != null && unitId > 0) {
            list = salaryStructureRepository.findByUnitId(unitId);
        } else {
            list = salaryStructureRepository.findAll();
        }
        return ApiResponse.success("SalaryStructures retrieved successfully", list);
    }
}
