package com.webicSoftIndia.HRMS.ApprovalAuthority;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalAuthorityRepository extends JpaRepository<ApprovalAuthority, Long> {
    List<ApprovalAuthority> findByUnitId(Long unitId);
    List<ApprovalAuthority> findByEmployeeId(Long employeeId);
}
