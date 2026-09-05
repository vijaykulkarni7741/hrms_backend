package com.webicSoftIndia.HRMS.Incentive;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncentiveRepository extends JpaRepository<Incentive, Long> {
    List<Incentive> findByUnitId(Long unitId);
    List<Incentive> findByEmployeeId(Long employeeId);
}
