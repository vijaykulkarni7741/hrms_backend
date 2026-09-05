package com.webicSoftIndia.HRMS.WorkflowHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowHistoryRepository extends JpaRepository<WorkflowHistory, Long> {
    List<WorkflowHistory> findByUnitId(Long unitId);
    List<WorkflowHistory> findByWorkflowId(Long workflowId);
}
