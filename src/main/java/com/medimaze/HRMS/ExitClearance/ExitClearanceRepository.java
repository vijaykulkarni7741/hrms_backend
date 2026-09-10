package com.medimaze.HRMS.ExitClearance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExitClearanceRepository extends JpaRepository<ExitClearance, Long> {
    List<ExitClearance> findByUnitId(Long unitId);
}
