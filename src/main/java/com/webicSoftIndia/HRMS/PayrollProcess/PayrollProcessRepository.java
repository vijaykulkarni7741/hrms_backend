package com.webicSoftIndia.HRMS.PayrollProcess;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollProcessRepository extends JpaRepository<PayrollProcess, Long> {
    List<PayrollProcess> findByUnitId(Long unitId);
    List<PayrollProcess> findByPayrollId(Long payrollId);
}
