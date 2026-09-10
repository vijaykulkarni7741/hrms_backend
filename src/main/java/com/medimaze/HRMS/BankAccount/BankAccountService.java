package com.medimaze.HRMS.BankAccount;

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
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<BankAccount> saveOrUpdateBankAccount(BankAccount bankAccount) {
        if (bankAccount == null) {
            return ApiResponse.error("Bank account payload cannot be null");
        }

        // UPDATE MODE
        if (bankAccount.getId() != null && bankAccount.getId() > 0) {
            BankAccount existing = bankAccountRepository.findById(bankAccount.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Bank account not found with ID: " + bankAccount.getId());
            }

            if (bankAccount.getAccountHolderName() != null && !bankAccount.getAccountHolderName().trim().isEmpty()) {
                existing.setAccountHolderName(bankAccount.getAccountHolderName().trim());
            }
            if (bankAccount.getAccountType() != null) {
                existing.setAccountType(bankAccount.getAccountType());
            }
            if (bankAccount.getIfscCode() != null) {
                existing.setIfscCode(bankAccount.getIfscCode());
            }
            if (bankAccount.getMicrCode() != null) {
                existing.setMicrCode(bankAccount.getMicrCode());
            }
            if (bankAccount.getUpiId() != null) {
                existing.setUpiId(bankAccount.getUpiId());
            }
            if (bankAccount.getSalaryAccount() != null) {
                existing.setSalaryAccount(bankAccount.getSalaryAccount());
            }
            if (bankAccount.getPrimaryAccount() != null) {
                existing.setPrimaryAccount(bankAccount.getPrimaryAccount());
            }
            if (bankAccount.getEffectiveFrom() != null) {
                existing.setEffectiveFrom(bankAccount.getEffectiveFrom());
            }
            if (bankAccount.getEffectiveTo() != null) {
                existing.setEffectiveTo(bankAccount.getEffectiveTo());
            }
            if (bankAccount.getVerificationStatus() != null) {
                existing.setVerificationStatus(bankAccount.getVerificationStatus());
            }
            if (bankAccount.getVerifiedBy() != null) {
                existing.setVerifiedBy(bankAccount.getVerifiedBy());
            }
            if (bankAccount.getVerifiedDate() != null) {
                existing.setVerifiedDate(bankAccount.getVerifiedDate());
            }
            if (bankAccount.getRemarks() != null) {
                existing.setRemarks(bankAccount.getRemarks());
            }
            if (bankAccount.getIsActive() != null) {
                existing.setIsActive(bankAccount.getIsActive());
            }

            BankAccount updated = bankAccountRepository.save(existing);
            log.info("Updated BankAccount ID: {}", updated.getId());
            return ApiResponse.success("Bank account updated successfully", updated);
        }

        // CREATE MODE
        if (bankAccount.getAccountNumber() == null || bankAccount.getAccountNumber().trim().isEmpty()) {
            return ApiResponse.error("Account number is required");
        }
        if (bankAccount.getAccountHolderName() == null || bankAccount.getAccountHolderName().trim().isEmpty()) {
            return ApiResponse.error("Account holder name is required");
        }

        if (bankAccountRepository.findByAccountNumber(bankAccount.getAccountNumber().trim()).isPresent()) {
            return ApiResponse.error("Account number already exists");
        }

        if (bankAccount.getIsActive() == null) {
            bankAccount.setIsActive(true);
        }

        BankAccount saved = bankAccountRepository.save(bankAccount);
        log.info("Created BankAccount ID: {}", saved.getId());
        return ApiResponse.success("Bank account created successfully", saved);
    }

    @Transactional
    public ApiResponse<BankAccount> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid BankAccount ID is required");
        }
        BankAccount bankAccount = bankAccountRepository.findById(id).orElse(null);
        if (bankAccount == null) {
            return ApiResponse.error("BankAccount not found with ID: " + id);
        }

        boolean currentStatus = Boolean.TRUE.equals(bankAccount.getIsActive());
        boolean newStatus = !currentStatus;
        bankAccount.setIsActive(newStatus);

        BankAccount updated = bankAccountRepository.save(bankAccount);
        log.info("Toggled active status for BankAccount ID: {} to {}", id, newStatus);
        return ApiResponse.success("Bank account active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<BankAccount>> getBankAccountsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<BankAccount> list = bankAccountRepository.findAll();
        return ApiResponse.success("Bank accounts retrieved successfully", list);
    }
}

