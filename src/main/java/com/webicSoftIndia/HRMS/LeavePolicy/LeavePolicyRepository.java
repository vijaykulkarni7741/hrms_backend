package com.webicSoftIndia.HRMS.LeavePolicy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeavePolicyRepository extends JpaRepository<LeavePolicy, Long> {
    List<LeavePolicy> findByUnitId(Long unitId);
}
