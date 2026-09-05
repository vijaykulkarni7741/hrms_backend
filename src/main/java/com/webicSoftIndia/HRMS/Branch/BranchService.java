package com.webicSoftIndia.HRMS.Branch;

import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Branch> saveOrUpdateBranch(Branch branch) {
        if (branch == null) {
            return ApiResponse.error("Branch payload cannot be null");
        }

        // UPDATE MODE
        if (branch.getId() != null && branch.getId() > 0) {
            Branch existing = branchRepository.findById(branch.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Branch not found with ID: " + branch.getId());
            }

            if (branch.getBranchName() != null && !branch.getBranchName().trim().isEmpty()) {
                existing.setBranchName(branch.getBranchName().trim());
            }
            if (branch.getBranchType() != null) {
                existing.setBranchType(branch.getBranchType());
            }
            if (branch.getAddressLine1() != null) {
                existing.setAddressLine1(branch.getAddressLine1());
            }
            if (branch.getAddressLine2() != null) {
                existing.setAddressLine2(branch.getAddressLine2());
            }
            if (branch.getCountryId() != null) {
                existing.setCountryId(branch.getCountryId());
            }
            if (branch.getStateId() != null) {
                existing.setStateId(branch.getStateId());
            }
            if (branch.getCity() != null) {
                existing.setCity(branch.getCity());
            }
            if (branch.getPinCode() != null) {
                existing.setPinCode(branch.getPinCode());
            }
            if (branch.getLatitude() != null) {
                existing.setLatitude(branch.getLatitude());
            }
            if (branch.getLongitude() != null) {
                existing.setLongitude(branch.getLongitude());
            }
            if (branch.getContactNumber() != null) {
                existing.setContactNumber(branch.getContactNumber());
            }
            if (branch.getEmail() != null) {
                existing.setEmail(branch.getEmail());
            }
            if (branch.getStatus() != null) {
                existing.setStatus(branch.getStatus());
            }
            if (branch.getIsActive() != null) {
                existing.setIsActive(branch.getIsActive());
            }

            Branch updated = branchRepository.save(existing);
            log.info("Updated Branch ID: {}", updated.getId());
            return ApiResponse.success("Branch updated successfully", updated);
        }

        // CREATE MODE
        if (branch.getBranchCode() == null || branch.getBranchCode().trim().isEmpty()) {
            return ApiResponse.error("Branch code is required");
        }
        if (branch.getBranchName() == null || branch.getBranchName().trim().isEmpty()) {
            return ApiResponse.error("Branch name is required");
        }

        if (branchRepository.findByBranchCode(branch.getBranchCode().trim()).isPresent()) {
            return ApiResponse.error("Branch code already exists");
        }

        if (branch.getStatus() == null) {
            branch.setStatus("ACTIVE");
        }
        if (branch.getIsActive() == null) {
            branch.setIsActive(true);
        }

        Branch saved = branchRepository.save(branch);
        log.info("Created Branch ID: {}", saved.getId());
        return ApiResponse.success("Branch created successfully", saved);
    }

    @Transactional
    public ApiResponse<Branch> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Branch ID is required");
        }
        Branch branch = branchRepository.findById(id).orElse(null);
        if (branch == null) {
            return ApiResponse.error("Branch not found with ID: " + id);
        }

        boolean currentStatus = Boolean.TRUE.equals(branch.getIsActive());
        boolean newStatus = !currentStatus;
        branch.setIsActive(newStatus);
        branch.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Branch updated = branchRepository.save(branch);
        log.info("Toggled active status for Branch ID: {} to {}", id, newStatus);
        return ApiResponse.success("Branch active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Branch>> getBranchesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<Branch> list;
        if (unitId != null && unitId > 0) {
            list = branchRepository.findByUnitId(unitId);
        } else {
            list = branchRepository.findAll();
        }
        return ApiResponse.success("Branches retrieved successfully", list);
    }
}

