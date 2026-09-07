package com.webicSoftIndia.HRMS.RuleConfig;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HrmsRuleConfigRepository extends JpaRepository<HrmsRuleConfig, Long> {
    Optional<HrmsRuleConfig> findFirstByUnitIdAndIsActiveTrue(Long unitId);
    Optional<HrmsRuleConfig> findFirstByIsActiveTrueOrderByCreatedDateDesc();
}
