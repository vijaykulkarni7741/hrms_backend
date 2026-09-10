package com.medimaze.HRMS.ESICConfiguration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ESICConfigurationRepository extends JpaRepository<ESICConfiguration, Long> {
    List<ESICConfiguration> findByUnitId(Long unitId);
}
