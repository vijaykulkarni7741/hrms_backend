package com.webicSoftIndia.HRMS.LeaveBalance;

import com.webicSoftIndia.HRMS.Employee.Employee;
import com.webicSoftIndia.HRMS.Employee.EmployeeRepository;
import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.LeaveType.LeaveType;
import com.webicSoftIndia.HRMS.LeaveType.LeaveTypeRepository;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<LeaveBalance> saveOrUpdateLeaveBalance(LeaveBalance leaveBalance) {
        if (leaveBalance == null) {
            return ApiResponse.error("LeaveBalance payload cannot be null");
        }

        if (leaveBalance.getId() != null && leaveBalance.getId() > 0) {
            LeaveBalance existing = leaveBalanceRepository.findById(leaveBalance.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("LeaveBalance not found with ID: " + leaveBalance.getId());
            }

            if (leaveBalance.getYear() != null) existing.setYear(leaveBalance.getYear());
            if (leaveBalance.getAllocatedDays() != null) existing.setAllocatedDays(leaveBalance.getAllocatedDays());
            if (leaveBalance.getUsedDays() != null) existing.setUsedDays(leaveBalance.getUsedDays());
            if (leaveBalance.getRemainingDays() != null) existing.setRemainingDays(leaveBalance.getRemainingDays());
            if (leaveBalance.getStatus() != null) existing.setStatus(leaveBalance.getStatus());
            if (leaveBalance.getIsActive() != null) existing.setIsActive(leaveBalance.getIsActive());

            LeaveBalance updated = leaveBalanceRepository.save(existing);
            return ApiResponse.success("LeaveBalance updated successfully", updated);
        }

        if (leaveBalance.getStatus() == null) leaveBalance.setStatus("ACTIVE");
        if (leaveBalance.getIsActive() == null) leaveBalance.setIsActive(true);

        LeaveBalance saved = leaveBalanceRepository.save(leaveBalance);
        return ApiResponse.success("LeaveBalance created successfully", saved);
    }

    @Transactional
    public ApiResponse<LeaveBalance> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid LeaveBalance ID is required");
        }
        LeaveBalance leaveBalance = leaveBalanceRepository.findById(id).orElse(null);
        if (leaveBalance == null) {
            return ApiResponse.error("LeaveBalance not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(leaveBalance.getIsActive());
        leaveBalance.setIsActive(newStatus);
        leaveBalance.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        LeaveBalance updated = leaveBalanceRepository.save(leaveBalance);
        return ApiResponse.success("LeaveBalance active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<LeaveBalance>> getLeaveBalancesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.success("Retrieved all leave balances", leaveBalanceRepository.findAll());
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.success("Retrieved all leave balances", leaveBalanceRepository.findAll());
        }

        Long unitId = sessionData.getUnitId();
        List<LeaveBalance> list;
        if (unitId != null && unitId > 0) {
            list = leaveBalanceRepository.findByUnitId(unitId);
        } else {
            list = leaveBalanceRepository.findAll();
        }
        return ApiResponse.success("LeaveBalances retrieved successfully", list);
    }

    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> getLeaveBalanceMasterData(String sessionId) {
        Map<String, Object> data = new HashMap<>();

        // 1. All Leave Types
        List<LeaveType> leaveTypes = leaveTypeRepository.findAll();
        data.put("leaveTypes", leaveTypes);

        // 2. All Employees
        List<Employee> employees = employeeRepository.findAll();
        data.put("employees", employees);

        // 3. Leave Balances
        List<LeaveBalance> balances;
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
            Long unitId = (sessionData != null) ? sessionData.getUnitId() : null;
            if (unitId != null && unitId > 0) {
                balances = leaveBalanceRepository.findByUnitId(unitId);
            } else {
                balances = leaveBalanceRepository.findAll();
            }
        } else {
            balances = leaveBalanceRepository.findAll();
        }
        data.put("balances", balances);

        return ApiResponse.success("Leave balance master data retrieved successfully in single API call", data);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<LeaveBalance>> getMyLeaveBalances(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.success("Retrieved leave balances", leaveBalanceRepository.findAll());
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null || sessionData.getId() == null) {
            return ApiResponse.success("Retrieved leave balances", leaveBalanceRepository.findAll());
        }

        Long empId = sessionData.getId();
        List<LeaveBalance> list = leaveBalanceRepository.findByEmployeeId(empId);
        return ApiResponse.success("Employee leave balances retrieved successfully", list);
    }

    @Transactional
    public ApiResponse<List<LeaveBalance>> saveBatchEmployeeBalances(Map<String, Object> payload) {
        if (payload == null) return ApiResponse.error("Payload cannot be null");

        Long empId = payload.get("employeeId") != null ? Long.valueOf(payload.get("employeeId").toString()) : null;
        String empCode = payload.get("employeeCode") != null ? payload.get("employeeCode").toString() : null;

        Employee emp = null;
        if (empId != null && empId > 0) {
            emp = employeeRepository.findById(empId).orElse(null);
        }
        if (emp == null && empCode != null && !empCode.trim().isEmpty()) {
            emp = employeeRepository.findByEmployeeCode(empCode.trim()).orElse(null);
        }
        if (emp == null) {
            return ApiResponse.error("Employee not found for given ID/Code");
        }

        int year = LocalDate.now().getYear();
        if (payload.get("year") != null) {
            try { year = Integer.parseInt(payload.get("year").toString()); } catch (Exception ignored) {}
        }

        Object balancesObj = payload.get("balances");
        if (!(balancesObj instanceof Map)) {
            return ApiResponse.error("Balances map object missing or invalid");
        }

        Map<?, ?> balancesMap = (Map<?, ?>) balancesObj;
        List<LeaveBalance> savedList = new ArrayList<>();

        for (Map.Entry<?, ?> entry : balancesMap.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) continue;
            String typeKey = entry.getKey().toString();
            BigDecimal allocated = new BigDecimal(entry.getValue().toString());

            LeaveType leaveType = null;
            try {
                Long ltId = Long.parseLong(typeKey);
                leaveType = leaveTypeRepository.findById(ltId).orElse(null);
            } catch (Exception e) {
                LeaveType foundByCode = leaveTypeRepository.findFirstByTypeCodeIgnoreCase(typeKey).orElse(null);
                leaveType = (foundByCode != null) ? foundByCode : leaveTypeRepository.findFirstByTypeNameIgnoreCase(typeKey).orElse(null);
            }

            if (leaveType == null) continue;

            final Employee targetEmp = emp;
            final LeaveType targetLeaveType = leaveType;
            final int targetYear = year;

            LeaveBalance lb = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(targetEmp.getId(), targetLeaveType.getId(), targetYear)
                    .orElseGet(() -> LeaveBalance.builder()
                            .organization(targetEmp.getOrganization())
                            .unit(targetEmp.getUnit())
                            .employee(targetEmp)
                            .leaveType(targetLeaveType)
                            .year(targetYear)
                            .usedDays(BigDecimal.ZERO)
                            .status("ACTIVE")
                            .isActive(true)
                            .build());

            lb.setAllocatedDays(allocated);
            BigDecimal used = lb.getUsedDays() != null ? lb.getUsedDays() : BigDecimal.ZERO;
            lb.setRemainingDays(allocated.subtract(used));
            savedList.add(leaveBalanceRepository.save(lb));
        }

        return ApiResponse.success("Employee leave balances saved successfully", savedList);
    }
}
