package com.medimaze.HRMS.PolicyApproval;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyApprovalRepository extends JpaRepository<PolicyApproval, Long> {
    List<PolicyApproval> findByUnitId(Long unitId);
}
