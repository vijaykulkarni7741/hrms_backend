package com.medimaze.HRMS.WorkflowStep;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, Long> {
    List<WorkflowStep> findByUnitId(Long unitId);
    List<WorkflowStep> findByWorkflowId(Long workflowId);
}
