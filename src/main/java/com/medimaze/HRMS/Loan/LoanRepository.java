package com.medimaze.HRMS.Loan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUnitId(Long unitId);
    List<Loan> findByEmployeeId(Long employeeId);
}
