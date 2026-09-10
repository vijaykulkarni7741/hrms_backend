package com.medimaze.HRMS.CostCenter;

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
public class CostCenterService {

    private final CostCenterRepository costCenterRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<CostCenter> saveOrUpdateCostCenter(CostCenter costCenter) {
        if (costCenter == null) {
            return ApiResponse.error("Cost center payload cannot be null");
        }

        // UPDATE MODE
        if (costCenter.getId() != null && costCenter.getId() > 0) {
            CostCenter existing = costCenterRepository.findById(costCenter.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Cost center not found with ID: " + costCenter.getId());
            }

            if (costCenter.getCostCenterName() != null && !costCenter.getCostCenterName().trim().isEmpty()) {
                existing.setCostCenterName(costCenter.getCostCenterName().trim());
            }
            if (costCenter.getDepartment() != null) {
                existing.setDepartment(costCenter.getDepartment());
            }
            if (costCenter.getManagerId() != null) {
                existing.setManagerId(costCenter.getManagerId());
            }
            if (costCenter.getBudgetAmount() != null) {
                existing.setBudgetAmount(costCenter.getBudgetAmount());
            }
            if (costCenter.getDescription() != null) {
                existing.setDescription(costCenter.getDescription());
            }
            if (costCenter.getStatus() != null) {
                existing.setStatus(costCenter.getStatus());
            }
            if (costCenter.getIsActive() != null) {
                existing.setIsActive(costCenter.getIsActive());
            }

            CostCenter updated = costCenterRepository.save(existing);
            log.info("Updated CostCenter ID: {}", updated.getId());
            return ApiResponse.success("Cost center updated successfully", updated);
        }

        // CREATE MODE
        if (costCenter.getCostCenterCode() == null || costCenter.getCostCenterCode().trim().isEmpty()) {
            return ApiResponse.error("Cost center code is required");
        }
        if (costCenter.getCostCenterName() == null || costCenter.getCostCenterName().trim().isEmpty()) {
            return ApiResponse.error("Cost center name is required");
        }

        if (costCenterRepository.findByCostCenterCode(costCenter.getCostCenterCode().trim()).isPresent()) {
            return ApiResponse.error("Cost center code already exists");
        }

        if (costCenter.getStatus() == null) {
            costCenter.setStatus("ACTIVE");
        }
        if (costCenter.getIsActive() == null) {
            costCenter.setIsActive(true);
        }

        CostCenter saved = costCenterRepository.save(costCenter);
        log.info("Created CostCenter ID: {}", saved.getId());
        return ApiResponse.success("Cost center created successfully", saved);
    }

    @Transactional
    public ApiResponse<CostCenter> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid CostCenter ID is required");
        }
        CostCenter costCenter = costCenterRepository.findById(id).orElse(null);
        if (costCenter == null) {
            return ApiResponse.error("CostCenter not found with ID: " + id);
        }

        boolean currentStatus = Boolean.TRUE.equals(costCenter.getIsActive());
        boolean newStatus = !currentStatus;
        costCenter.setIsActive(newStatus);
        costCenter.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        CostCenter updated = costCenterRepository.save(costCenter);
        log.info("Toggled active status for CostCenter ID: {} to {}", id, newStatus);
        return ApiResponse.success("Cost center active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<CostCenter>> getCostCentersBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<CostCenter> list;
        if (unitId != null && unitId > 0) {
            list = costCenterRepository.findByUnitId(unitId);
        } else {
            list = costCenterRepository.findAll();
        }
        return ApiResponse.success("Cost centers retrieved successfully", list);
    }
}

