package com.webicSoftIndia.HRMS.Leave;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByUnitId(Long unitId);
    List<Leave> findByEmployeeId(Long employeeId);
}
