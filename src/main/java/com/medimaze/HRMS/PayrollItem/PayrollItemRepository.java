package com.medimaze.HRMS.PayrollItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayrollItemRepository extends JpaRepository<PayrollItem, Long> {
    List<PayrollItem> findByUnitId(Long unitId);
    List<PayrollItem> findByPayrollId(Long payrollId);
}
