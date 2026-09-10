package com.medimaze.HRMS.PFConfiguration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PFConfigurationRepository extends JpaRepository<PFConfiguration, Long> {
    List<PFConfiguration> findByUnitId(Long unitId);
}
