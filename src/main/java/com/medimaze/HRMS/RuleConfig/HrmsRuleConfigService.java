package com.medimaze.HRMS.RuleConfig;

import com.medimaze.HRMS.Employee.dto.EmployeeDto;
import com.medimaze.HRMS.Unit.Unit;
import com.medimaze.HRMS.Unit.UnitRepository;
import com.medimaze.HRMS.common.ApiResponse;
import com.medimaze.HRMS.security.RedisSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class HrmsRuleConfigService {

    private final HrmsRuleConfigRepository ruleConfigRepository;
    private final UnitRepository unitRepository;
    private final RedisSessionService redisSessionService;

    public HrmsRuleConfigService(
            HrmsRuleConfigRepository ruleConfigRepository,
            UnitRepository unitRepository,
            RedisSessionService redisSessionService) {
        this.ruleConfigRepository = ruleConfigRepository;
        this.unitRepository = unitRepository;
        this.redisSessionService = redisSessionService;
    }

    @Transactional(readOnly = true)
    public HrmsRuleConfig getEffectiveRuleConfig(Long unitId) {
        if (unitId != null && unitId > 0) {
            Optional<HrmsRuleConfig> unitConfig = ruleConfigRepository.findFirstByUnitIdAndIsActiveTrue(unitId);
            if (unitConfig.isPresent()) {
                return unitConfig.get();
            }
        }
        HrmsRuleConfig config = ruleConfigRepository.findFirstByIsActiveTrueOrderByCreatedDateDesc()
                .orElseGet(() -> HrmsRuleConfig.builder()
                        .plannedLeaveAdvanceDays(5)
                        .regularizationMaxDays(7)
                        .maxRegularizationsPerMonth(3)
                        .maxLeaveApplicationsPerMonth(10)
                        .allowSameDayRegularization(true)
                        .allowFutureDateRegularization(false)
                        .standardInTime("09:30")
                        .standardOutTime("18:30")
                        .isActive(true)
                        .build());
        
        // Ensure defaults if any property is null
        if (config.getMaxRegularizationsPerMonth() == null) config.setMaxRegularizationsPerMonth(3);
        if (config.getMaxLeaveApplicationsPerMonth() == null) config.setMaxLeaveApplicationsPerMonth(10);
        if (config.getAllowSameDayRegularization() == null) config.setAllowSameDayRegularization(true);
        if (config.getAllowFutureDateRegularization() == null) config.setAllowFutureDateRegularization(false);
        return config;
    }

    @Transactional(readOnly = true)
    public ApiResponse<HrmsRuleConfig> getRuleConfigBySession(String sessionId) {
        Long unitId = null;
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
            if (sessionData != null) {
                unitId = sessionData.getUnitId();
            }
        }
        HrmsRuleConfig config = getEffectiveRuleConfig(unitId);
        return ApiResponse.success("HR policy rule configuration retrieved successfully", config);
    }

    @Transactional
    public ApiResponse<HrmsRuleConfig> saveOrUpdateRuleConfig(HrmsRuleConfig request, String sessionId) {
        if (request == null) {
            return ApiResponse.error("Rule configuration payload cannot be null");
        }

        String updatedBy = "HR Manager";
        Long unitId = null;
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
            if (sessionData != null) {
                if (sessionData.getFirstName() != null) {
                    updatedBy = sessionData.getFirstName() + " " + (sessionData.getLastName() != null ? sessionData.getLastName() : "");
                }
                unitId = sessionData.getUnitId();
            }
        }

        HrmsRuleConfig configToSave;
        if (request.getId() != null && request.getId() > 0) {
            configToSave = ruleConfigRepository.findById(request.getId()).orElse(request);
        } else if (unitId != null && unitId > 0) {
            configToSave = ruleConfigRepository.findFirstByUnitIdAndIsActiveTrue(unitId)
                    .orElseGet(HrmsRuleConfig::new);
        } else {
            configToSave = ruleConfigRepository.findFirstByIsActiveTrueOrderByCreatedDateDesc()
                    .orElseGet(HrmsRuleConfig::new);
        }

        if (request.getPlannedLeaveAdvanceDays() != null && request.getPlannedLeaveAdvanceDays() >= 0) {
            configToSave.setPlannedLeaveAdvanceDays(request.getPlannedLeaveAdvanceDays());
        }
        if (request.getRegularizationMaxDays() != null && request.getRegularizationMaxDays() >= 0) {
            configToSave.setRegularizationMaxDays(request.getRegularizationMaxDays());
        }
        if (request.getMaxRegularizationsPerMonth() != null && request.getMaxRegularizationsPerMonth() >= 0) {
            configToSave.setMaxRegularizationsPerMonth(request.getMaxRegularizationsPerMonth());
        }
        if (request.getMaxLeaveApplicationsPerMonth() != null && request.getMaxLeaveApplicationsPerMonth() >= 0) {
            configToSave.setMaxLeaveApplicationsPerMonth(request.getMaxLeaveApplicationsPerMonth());
        }
        if (request.getAllowSameDayRegularization() != null) {
            configToSave.setAllowSameDayRegularization(request.getAllowSameDayRegularization());
        }
        if (request.getAllowFutureDateRegularization() != null) {
            configToSave.setAllowFutureDateRegularization(request.getAllowFutureDateRegularization());
        }
        if (request.getStandardInTime() != null && !request.getStandardInTime().trim().isEmpty()) {
            configToSave.setStandardInTime(request.getStandardInTime().trim());
        }
        if (request.getStandardOutTime() != null && !request.getStandardOutTime().trim().isEmpty()) {
            configToSave.setStandardOutTime(request.getStandardOutTime().trim());
        }

        if (configToSave.getUnit() == null && unitId != null && unitId > 0) {
            Unit unit = unitRepository.findById(unitId).orElse(null);
            configToSave.setUnit(unit);
        }

        configToSave.setIsActive(true);
        configToSave.setUpdatedBy(updatedBy);

        HrmsRuleConfig saved = ruleConfigRepository.save(configToSave);
        log.info("HR Rule Config updated successfully by {}: PlannedLeaveAdvanceDays={}, RegularizationMaxDays={}, MaxRegsPerMonth={}, MaxLeavesPerMonth={}",
                updatedBy, saved.getPlannedLeaveAdvanceDays(), saved.getRegularizationMaxDays(), saved.getMaxRegularizationsPerMonth(), saved.getMaxLeaveApplicationsPerMonth());
        return ApiResponse.success("HR policy rule configuration saved successfully", saved);
    }
}
