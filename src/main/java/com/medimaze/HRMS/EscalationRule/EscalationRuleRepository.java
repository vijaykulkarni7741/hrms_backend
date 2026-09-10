package com.medimaze.HRMS.EscalationRule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscalationRuleRepository extends JpaRepository<EscalationRule, Long> {
    List<EscalationRule> findByUnitId(Long unitId);
    List<EscalationRule> findByWorkflowStepId(Long workflowStepId);
}
