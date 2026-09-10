package com.medimaze.HRMS.PolicyConfiguration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyConfigurationRepository extends JpaRepository<PolicyConfiguration, Long> {
    List<PolicyConfiguration> findByUnitId(Long unitId);
    List<PolicyConfiguration> findByPolicyId(Long policyId);
}
