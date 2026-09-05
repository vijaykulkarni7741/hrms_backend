package com.webicSoftIndia.HRMS.CostCenter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CostCenterRepository extends JpaRepository<CostCenter, Long> {
    Optional<CostCenter> findByCostCenterCode(String costCenterCode);
    List<CostCenter> findByUnitId(Long unitId);
}

