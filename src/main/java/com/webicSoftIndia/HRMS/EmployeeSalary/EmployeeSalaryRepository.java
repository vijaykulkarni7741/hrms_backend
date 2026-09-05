package com.webicSoftIndia.HRMS.EmployeeSalary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeSalaryRepository extends JpaRepository<EmployeeSalary, Long> {
    List<EmployeeSalary> findByUnitId(Long unitId);
    List<EmployeeSalary> findByEmployeeId(Long employeeId);
}
