package com.webicSoftIndia.HRMS.Bank;

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
public class BankService {

    private final BankRepository bankRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Bank> saveOrUpdateBank(Bank bank) {
        if (bank == null) {
            return ApiResponse.error("Bank payload cannot be null");
        }

        // UPDATE MODE
        if (bank.getId() != null && bank.getId() > 0) {
            Bank existing = bankRepository.findById(bank.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Bank not found with ID: " + bank.getId());
            }

            if (bank.getBankName() != null && !bank.getBankName().trim().isEmpty()) {
                existing.setBankName(bank.getBankName().trim());
            }
            if (bank.getShortName() != null) {
                existing.setShortName(bank.getShortName());
            }
            if (bank.getIfscPrefix() != null) {
                existing.setIfscPrefix(bank.getIfscPrefix());
            }
            if (bank.getSwiftCode() != null) {
                existing.setSwiftCode(bank.getSwiftCode());
            }
            if (bank.getWebsite() != null) {
                existing.setWebsite(bank.getWebsite());
            }
            if (bank.getCustomerCareNumber() != null) {
                existing.setCustomerCareNumber(bank.getCustomerCareNumber());
            }
            if (bank.getStatus() != null) {
                existing.setStatus(bank.getStatus());
            }
            if (bank.getIsActive() != null) {
                existing.setIsActive(bank.getIsActive());
            }

            Bank updated = bankRepository.save(existing);
            log.info("Updated Bank ID: {}", updated.getId());
            return ApiResponse.success("Bank updated successfully", updated);
        }

        // CREATE MODE
        if (bank.getBankCode() == null || bank.getBankCode().trim().isEmpty()) {
            return ApiResponse.error("Bank code is required");
        }
        if (bank.getBankName() == null || bank.getBankName().trim().isEmpty()) {
            return ApiResponse.error("Bank name is required");
        }

        if (bankRepository.findByBankCode(bank.getBankCode().trim()).isPresent()) {
            return ApiResponse.error("Bank code already exists");
        }

        if (bank.getStatus() == null) {
            bank.setStatus("ACTIVE");
        }
        if (bank.getIsActive() == null) {
            bank.setIsActive(true);
        }

        Bank saved = bankRepository.save(bank);
        log.info("Created Bank ID: {}", saved.getId());
        return ApiResponse.success("Bank created successfully", saved);
    }

    @Transactional
    public ApiResponse<Bank> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Bank ID is required");
        }
        Bank bank = bankRepository.findById(id).orElse(null);
        if (bank == null) {
            return ApiResponse.error("Bank not found with ID: " + id);
        }

        boolean currentStatus = Boolean.TRUE.equals(bank.getIsActive());
        boolean newStatus = !currentStatus;
        bank.setIsActive(newStatus);
        bank.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Bank updated = bankRepository.save(bank);
        log.info("Toggled active status for Bank ID: {} to {}", id, newStatus);
        return ApiResponse.success("Bank active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Bank>> getBanksBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        List<Bank> list = bankRepository.findAll();
        return ApiResponse.success("Banks retrieved successfully", list);
    }
}

