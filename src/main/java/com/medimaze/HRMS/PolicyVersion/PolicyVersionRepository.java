package com.medimaze.HRMS.PolicyVersion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyVersionRepository extends JpaRepository<PolicyVersion, Long> {
    List<PolicyVersion> findByUnitId(Long unitId);
    List<PolicyVersion> findByPolicyId(Long policyId);
}
