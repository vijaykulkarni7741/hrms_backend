package com.webicSoftIndia.HRMS.Bonus;

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
public class BonusService {

    private final BonusRepository bonusRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Bonus> saveOrUpdateBonus(Bonus bonus) {
        if (bonus == null) {
            return ApiResponse.error("Bonus payload cannot be null");
        }

        if (bonus.getId() != null && bonus.getId() > 0) {
            Bonus existing = bonusRepository.findById(bonus.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Bonus not found with ID: " + bonus.getId());
            }

            if (bonus.getBonusName() != null) existing.setBonusName(bonus.getBonusName());
            if (bonus.getAmount() != null) existing.setAmount(bonus.getAmount());
            if (bonus.getPayoutDate() != null) existing.setPayoutDate(bonus.getPayoutDate());
            if (bonus.getStatus() != null) existing.setStatus(bonus.getStatus());
            if (bonus.getIsActive() != null) existing.setIsActive(bonus.getIsActive());

            Bonus updated = bonusRepository.save(existing);
            return ApiResponse.success("Bonus updated successfully", updated);
        }

        if (bonus.getBonusName() == null || bonus.getBonusName().trim().isEmpty()) {
            return ApiResponse.error("Bonus name is required");
        }

        if (bonus.getStatus() == null) bonus.setStatus("PENDING");
        if (bonus.getIsActive() == null) bonus.setIsActive(true);

        Bonus saved = bonusRepository.save(bonus);
        return ApiResponse.success("Bonus created successfully", saved);
    }

    @Transactional
    public ApiResponse<Bonus> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Bonus ID is required");
        }
        Bonus bonus = bonusRepository.findById(id).orElse(null);
        if (bonus == null) {
            return ApiResponse.error("Bonus not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(bonus.getIsActive());
        bonus.setIsActive(newStatus);
        bonus.setStatus(newStatus ? "PENDING" : "CANCELLED");

        Bonus updated = bonusRepository.save(bonus);
        return ApiResponse.success("Bonus active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Bonus>> getBonusesBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<Bonus> list;
        if (unitId != null && unitId > 0) {
            list = bonusRepository.findByUnitId(unitId);
        } else {
            list = bonusRepository.findAll();
        }
        return ApiResponse.success("Bonuses retrieved successfully", list);
    }
}
