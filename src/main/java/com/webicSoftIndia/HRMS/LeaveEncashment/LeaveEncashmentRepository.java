package com.webicSoftIndia.HRMS.LeaveEncashment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveEncashmentRepository extends JpaRepository<LeaveEncashment, Long> {
    List<LeaveEncashment> findByUnitId(Long unitId);
    List<LeaveEncashment> findByEmployeeId(Long employeeId);
}
