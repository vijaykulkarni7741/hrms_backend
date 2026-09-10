package com.medimaze.HRMS.WorkflowApproval;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowApprovalRepository extends JpaRepository<WorkflowApproval, Long> {
    List<WorkflowApproval> findByUnitId(Long unitId);
    List<WorkflowApproval> findByWorkflowStepId(Long workflowStepId);
}
