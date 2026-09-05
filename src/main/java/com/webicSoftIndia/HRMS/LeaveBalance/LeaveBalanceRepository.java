package com.webicSoftIndia.HRMS.LeaveBalance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    List<LeaveBalance> findByUnitId(Long unitId);
    List<LeaveBalance> findByEmployeeId(Long employeeId);
}
