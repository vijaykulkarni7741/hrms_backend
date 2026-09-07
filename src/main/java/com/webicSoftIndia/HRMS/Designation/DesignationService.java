package com.webicSoftIndia.HRMS.Designation;

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
public class DesignationService {

    private final DesignationRepository designationRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Designation> saveOrUpdateDesignation(Designation designation) {
        if (designation == null) {
            return ApiResponse.error("Designation payload cannot be null");
        }

        // UPDATE MODE
        if (designation.getId() != null && designation.getId() > 0) {
            Designation existing = designationRepository.findById(designation.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Designation not found with ID: " + designation.getId());
            }

            if (designation.getDesignationName() != null && !designation.getDesignationName().trim().isEmpty()) {
                existing.setDesignationName(designation.getDesignationName().trim());
            }
            if (designation.getStatus() != null) {
                existing.setStatus(designation.getStatus());
            }
            if (designation.getIsActive() != null) {
                existing.setIsActive(designation.getIsActive());
            }

            Designation updated = designationRepository.save(existing);
            log.info("Updated Designation ID: {}", updated.getId());
            return ApiResponse.success("Designation updated successfully", updated);
        }

        // CREATE MODE
        if (designation.getDesignationCode() == null || designation.getDesignationCode().trim().isEmpty()) {
            return ApiResponse.error("Designation code is required");
        }
        if (designation.getDesignationName() == null || designation.getDesignationName().trim().isEmpty()) {
            return ApiResponse.error("Designation name is required");
        }

        if (designationRepository.findByDesignationCode(designation.getDesignationCode().trim()).isPresent()) {
            return ApiResponse.error("Designation code already exists");
        }

        if (designation.getStatus() == null) {
            designation.setStatus("ACTIVE");
        }
        if (designation.getIsActive() == null) {
            designation.setIsActive(true);
        }

        Designation saved = designationRepository.save(designation);
        log.info("Created Designation ID: {}", saved.getId());
        return ApiResponse.success("Designation created successfully", saved);
    }

    @Transactional
    public ApiResponse<Designation> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Designation ID is required");
        }
        Designation designation = designationRepository.findById(id).orElse(null);
        if (designation == null) {
            return ApiResponse.error("Designation not found with ID: " + id);
        }

        boolean currentStatus = Boolean.TRUE.equals(designation.getIsActive());
        boolean newStatus = !currentStatus;
        designation.setIsActive(newStatus);
        designation.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Designation updated = designationRepository.save(designation);
        log.info("Toggled active status for Designation ID: {} to {}", id, newStatus);
        return ApiResponse.success("Designation active status toggled successfully to " + newStatus, updated);
    }

    @Transactional
    public void deleteDesignation(Long id) {
        if (id != null && designationRepository.existsById(id)) {
            designationRepository.deleteById(id);
        }
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Designation>> getDesignationsBySessionUnit(String sessionId) {
        List<Designation> list = designationRepository.findAll();
        return ApiResponse.success("Designations retrieved successfully", list);
    }
}

