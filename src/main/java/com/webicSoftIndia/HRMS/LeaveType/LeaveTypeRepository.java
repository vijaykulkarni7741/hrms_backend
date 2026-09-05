package com.webicSoftIndia.HRMS.LeaveType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    List<LeaveType> findByUnitId(Long unitId);
}
