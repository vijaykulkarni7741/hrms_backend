package com.webicSoftIndia.HRMS.FnFSettlement;

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
public class FnFSettlementService {

    private final FnFSettlementRepository fnfSettlementRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<FnFSettlement> saveOrUpdateFnFSettlement(FnFSettlement settlement) {
        if (settlement == null) {
            return ApiResponse.error("FnFSettlement payload cannot be null");
        }

        if (settlement.getId() != null && settlement.getId() > 0) {
            FnFSettlement existing = fnfSettlementRepository.findById(settlement.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("FnFSettlement record not found with ID: " + settlement.getId());
            }

            if (settlement.getBasicPayAmount() != null) existing.setBasicPayAmount(settlement.getBasicPayAmount());
            if (settlement.getLeaveEncashmentAmount() != null) existing.setLeaveEncashmentAmount(settlement.getLeaveEncashmentAmount());
            if (settlement.getGratuityAmount() != null) existing.setGratuityAmount(settlement.getGratuityAmount());
            if (settlement.getDeductionsAmount() != null) existing.setDeductionsAmount(settlement.getDeductionsAmount());
            if (settlement.getNetPayableAmount() != null) existing.setNetPayableAmount(settlement.getNetPayableAmount());
            if (settlement.getSettlementDate() != null) existing.setSettlementDate(settlement.getSettlementDate());
            if (settlement.getPaymentMode() != null) existing.setPaymentMode(settlement.getPaymentMode());
            if (settlement.getRemarks() != null) existing.setRemarks(settlement.getRemarks());
            if (settlement.getStatus() != null) existing.setStatus(settlement.getStatus());
            if (settlement.getIsActive() != null) existing.setIsActive(settlement.getIsActive());

            FnFSettlement updated = fnfSettlementRepository.save(existing);
            return ApiResponse.success("FnFSettlement updated successfully", updated);
        }

        if (settlement.getStatus() == null) settlement.setStatus("DRAFT");
        if (settlement.getIsActive() == null) settlement.setIsActive(true);

        FnFSettlement saved = fnfSettlementRepository.save(settlement);
        return ApiResponse.success("FnFSettlement created successfully", saved);
    }

    @Transactional
    public ApiResponse<FnFSettlement> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid FnFSettlement ID is required");
        }
        FnFSettlement settlement = fnfSettlementRepository.findById(id).orElse(null);
        if (settlement == null) {
            return ApiResponse.error("FnFSettlement record not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(settlement.getIsActive());
        settlement.setIsActive(newStatus);
        settlement.setStatus(newStatus ? "DRAFT" : "INACTIVE");

        FnFSettlement updated = fnfSettlementRepository.save(settlement);
        return ApiResponse.success("FnFSettlement active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<FnFSettlement>> getFnFSettlementsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<FnFSettlement> list;
        if (unitId != null && unitId > 0) {
            list = fnfSettlementRepository.findByUnitId(unitId);
        } else {
            list = fnfSettlementRepository.findAll();
        }
        return ApiResponse.success("FnFSettlements retrieved successfully", list);
    }
}
