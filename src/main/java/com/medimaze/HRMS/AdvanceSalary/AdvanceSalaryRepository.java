package com.medimaze.HRMS.AdvanceSalary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvanceSalaryRepository extends JpaRepository<AdvanceSalary, Long> {
    List<AdvanceSalary> findByUnitId(Long unitId);
    List<AdvanceSalary> findByEmployeeId(Long employeeId);
}
