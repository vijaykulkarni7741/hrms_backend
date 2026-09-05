package com.webicSoftIndia.HRMS.AssetAllocation;

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
public class AssetAllocationService {

    private final AssetAllocationRepository assetAllocationRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<AssetAllocation> saveOrUpdateAssetAllocation(AssetAllocation allocation) {
        if (allocation == null) {
            return ApiResponse.error("AssetAllocation payload cannot be null");
        }

        if (allocation.getId() != null && allocation.getId() > 0) {
            AssetAllocation existing = assetAllocationRepository.findById(allocation.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("AssetAllocation record not found with ID: " + allocation.getId());
            }

            if (allocation.getAssetName() != null) existing.setAssetName(allocation.getAssetName());
            if (allocation.getAssetCode() != null) existing.setAssetCode(allocation.getAssetCode());
            if (allocation.getSerialNumber() != null) existing.setSerialNumber(allocation.getSerialNumber());
            if (allocation.getAllocationDate() != null) existing.setAllocationDate(allocation.getAllocationDate());
            if (allocation.getReturnDate() != null) existing.setReturnDate(allocation.getReturnDate());
            if (allocation.getConditionOnAllocation() != null) existing.setConditionOnAllocation(allocation.getConditionOnAllocation());
            if (allocation.getConditionOnReturn() != null) existing.setConditionOnReturn(allocation.getConditionOnReturn());
            if (allocation.getStatus() != null) existing.setStatus(allocation.getStatus());
            if (allocation.getIsActive() != null) existing.setIsActive(allocation.getIsActive());

            AssetAllocation updated = assetAllocationRepository.save(existing);
            return ApiResponse.success("AssetAllocation updated successfully", updated);
        }

        if (allocation.getAssetName() == null || allocation.getAssetName().trim().isEmpty()) {
            return ApiResponse.error("Asset name is required");
        }
        if (allocation.getAllocationDate() == null) {
            return ApiResponse.error("Allocation date is required");
        }

        if (allocation.getStatus() == null) allocation.setStatus("ALLOCATED");
        if (allocation.getIsActive() == null) allocation.setIsActive(true);

        AssetAllocation saved = assetAllocationRepository.save(allocation);
        return ApiResponse.success("AssetAllocation created successfully", saved);
    }

    @Transactional
    public ApiResponse<AssetAllocation> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid AssetAllocation ID is required");
        }
        AssetAllocation allocation = assetAllocationRepository.findById(id).orElse(null);
        if (allocation == null) {
            return ApiResponse.error("AssetAllocation record not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(allocation.getIsActive());
        allocation.setIsActive(newStatus);
        allocation.setStatus(newStatus ? "ALLOCATED" : "INACTIVE");

        AssetAllocation updated = assetAllocationRepository.save(allocation);
        return ApiResponse.success("AssetAllocation active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<AssetAllocation>> getAssetAllocationsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<AssetAllocation> list;
        if (unitId != null && unitId > 0) {
            list = assetAllocationRepository.findByUnitId(unitId);
        } else {
            list = assetAllocationRepository.findAll();
        }
        return ApiResponse.success("AssetAllocations retrieved successfully", list);
    }
}
