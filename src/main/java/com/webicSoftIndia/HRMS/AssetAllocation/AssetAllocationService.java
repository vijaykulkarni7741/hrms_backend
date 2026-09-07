package com.webicSoftIndia.HRMS.AssetAllocation;

import com.webicSoftIndia.HRMS.Employee.Employee;
import com.webicSoftIndia.HRMS.Employee.EmployeeRepository;
import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.Organization.Organization;
import com.webicSoftIndia.HRMS.Organization.OrganizationRepository;
import com.webicSoftIndia.HRMS.Unit.Unit;
import com.webicSoftIndia.HRMS.Unit.UnitRepository;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetAllocationService {

    private final AssetAllocationRepository assetAllocationRepository;
    private final RedisSessionService redisSessionService;
    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final UnitRepository unitRepository;

    @Transactional
    public ApiResponse<AssetAllocation> createOrUpdateFromMap(Map<String, Object> payload, String sessionId) {
        if (payload == null) {
            return ApiResponse.error("Payload cannot be null");
        }

        Long id = payload.get("id") != null ? Long.valueOf(payload.get("id").toString()) : null;
        Long employeeId = payload.get("employeeId") != null ? Long.valueOf(payload.get("employeeId").toString()) : null;
        String assetName = payload.get("assetName") != null ? payload.get("assetName").toString() : null;
        String assetCode = payload.get("assetCode") != null ? payload.get("assetCode").toString() : null;
        String serialNumber = payload.get("serialNumber") != null ? payload.get("serialNumber").toString() : null;
        String conditionOnAllocation = payload.get("conditionOnAllocation") != null ? payload.get("conditionOnAllocation").toString() : null;
        String conditionOnReturn = payload.get("conditionOnReturn") != null ? payload.get("conditionOnReturn").toString() : null;
        String status = payload.get("status") != null ? payload.get("status").toString() : "ALLOCATED";

        LocalDate allocationDate = LocalDate.now();
        if (payload.get("allocationDate") != null && !payload.get("allocationDate").toString().trim().isEmpty()) {
            try {
                allocationDate = LocalDate.parse(payload.get("allocationDate").toString().substring(0, 10));
            } catch (Exception ignored) {}
        }

        LocalDate returnDate = null;
        if (payload.get("returnDate") != null && !payload.get("returnDate").toString().trim().isEmpty()) {
            try {
                returnDate = LocalDate.parse(payload.get("returnDate").toString().substring(0, 10));
            } catch (Exception ignored) {}
        }

        AssetAllocation allocation;
        if (id != null && id > 0) {
            allocation = assetAllocationRepository.findById(id).orElse(new AssetAllocation());
        } else {
            allocation = new AssetAllocation();
        }

        if (assetName == null || assetName.trim().isEmpty()) {
            return ApiResponse.error("Asset name is required");
        }

        allocation.setAssetName(assetName);
        allocation.setAssetCode(assetCode != null ? assetCode : "AST-" + System.currentTimeMillis() % 10000);
        allocation.setSerialNumber(serialNumber);
        allocation.setAllocationDate(allocationDate);
        allocation.setReturnDate(returnDate);
        allocation.setConditionOnAllocation(conditionOnAllocation != null ? conditionOnAllocation : "GOOD");
        allocation.setConditionOnReturn(conditionOnReturn);
        allocation.setStatus(status);
        allocation.setIsActive(!"INACTIVE".equalsIgnoreCase(status));

        // Bind Employee
        Employee employee = null;
        if (employeeId != null && employeeId > 0) {
            employee = employeeRepository.findById(employeeId).orElse(null);
        }
        if (employee == null) {
            employee = employeeRepository.findAll().stream().findFirst().orElse(null);
        }
        if (employee != null) {
            allocation.setEmployee(employee);
            if (employee.getOrganization() != null) allocation.setOrganization(employee.getOrganization());
            if (employee.getUnit() != null) allocation.setUnit(employee.getUnit());
        }

        // Fallback Org and Unit if not set
        if (allocation.getOrganization() == null) {
            Organization org = organizationRepository.findAll().stream().findFirst().orElse(null);
            allocation.setOrganization(org);
        }
        if (allocation.getUnit() == null) {
            Unit unit = unitRepository.findAll().stream().findFirst().orElse(null);
            allocation.setUnit(unit);
        }

        if (allocation.getOrganization() == null || allocation.getUnit() == null || allocation.getEmployee() == null) {
            return ApiResponse.error("Employee, Organization, and Unit must be valid to allocate asset.");
        }

        AssetAllocation saved = assetAllocationRepository.save(allocation);
        return ApiResponse.success("Asset allocation saved successfully", saved);
    }

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
            allocation.setAllocationDate(LocalDate.now());
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
        List<AssetAllocation> list = assetAllocationRepository.findAll();
        return ApiResponse.success("AssetAllocations retrieved successfully", list);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<AssetAllocation>> getAssetAllocationsByEmployee(Long employeeId, String sessionId) {
        if (employeeId != null && employeeId > 0) {
            List<AssetAllocation> list = assetAllocationRepository.findByEmployeeId(employeeId);
            return ApiResponse.success("AssetAllocations retrieved successfully", list);
        }

        if (sessionId != null && !sessionId.trim().isEmpty()) {
            EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
            if (sessionData != null && sessionData.getId() != null) {
                List<AssetAllocation> list = assetAllocationRepository.findByEmployeeId(sessionData.getId());
                if (!list.isEmpty()) {
                    return ApiResponse.success("AssetAllocations retrieved for session employee", list);
                }
            }
        }

        // Return all assets if no specific filter matches
        List<AssetAllocation> list = assetAllocationRepository.findAll();
        return ApiResponse.success("AssetAllocations retrieved successfully", list);
    }
}
