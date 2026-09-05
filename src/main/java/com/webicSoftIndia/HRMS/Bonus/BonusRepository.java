package com.webicSoftIndia.HRMS.Bonus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BonusRepository extends JpaRepository<Bonus, Long> {
    List<Bonus> findByUnitId(Long unitId);
    List<Bonus> findByEmployeeId(Long employeeId);
}
