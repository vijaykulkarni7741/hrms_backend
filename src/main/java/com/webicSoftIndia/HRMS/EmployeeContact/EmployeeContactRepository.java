package com.webicSoftIndia.HRMS.EmployeeContact;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeContactRepository extends JpaRepository<EmployeeContact, Long> {
    List<EmployeeContact> findByEmployeeId(Long employeeId);
}
