package com.webicSoftIndia.HRMS.SalarySlip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalarySlipRepository extends JpaRepository<SalarySlip, Long> {
    List<SalarySlip> findByUnitId(Long unitId);
    List<SalarySlip> findByEmployeeId(Long employeeId);
}
