package com.webicSoftIndia.HRMS.Loan;

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
public class LoanService {

    private final LoanRepository loanRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Loan> saveOrUpdateLoan(Loan loan) {
        if (loan == null) {
            return ApiResponse.error("Loan payload cannot be null");
        }

        if (loan.getId() != null && loan.getId() > 0) {
            Loan existing = loanRepository.findById(loan.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Loan not found with ID: " + loan.getId());
            }

            if (loan.getLoanAmount() != null) existing.setLoanAmount(loan.getLoanAmount());
            if (loan.getRepaymentMonths() != null) existing.setRepaymentMonths(loan.getRepaymentMonths());
            if (loan.getMonthlyEmi() != null) existing.setMonthlyEmi(loan.getMonthlyEmi());
            if (loan.getDisbursedDate() != null) existing.setDisbursedDate(loan.getDisbursedDate());
            if (loan.getStatus() != null) existing.setStatus(loan.getStatus());
            if (loan.getIsActive() != null) existing.setIsActive(loan.getIsActive());

            Loan updated = loanRepository.save(existing);
            return ApiResponse.success("Loan updated successfully", updated);
        }

        if (loan.getLoanAmount() == null) {
            return ApiResponse.error("Loan amount is required");
        }

        if (loan.getStatus() == null) loan.setStatus("APPLIED");
        if (loan.getIsActive() == null) loan.setIsActive(true);

        Loan saved = loanRepository.save(loan);
        return ApiResponse.success("Loan created successfully", saved);
    }

    @Transactional
    public ApiResponse<Loan> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Loan ID is required");
        }
        Loan loan = loanRepository.findById(id).orElse(null);
        if (loan == null) {
            return ApiResponse.error("Loan not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(loan.getIsActive());
        loan.setIsActive(newStatus);
        loan.setStatus(newStatus ? "APPLIED" : "CLOSED");

        Loan updated = loanRepository.save(loan);
        return ApiResponse.success("Loan active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Loan>> getLoansBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<Loan> list;
        if (unitId != null && unitId > 0) {
            list = loanRepository.findByUnitId(unitId);
        } else {
            list = loanRepository.findAll();
        }
        return ApiResponse.success("Loans retrieved successfully", list);
    }
}
