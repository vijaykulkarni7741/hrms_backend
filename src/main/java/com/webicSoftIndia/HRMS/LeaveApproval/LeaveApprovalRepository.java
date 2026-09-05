package com.webicSoftIndia.HRMS.LeaveApproval;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveApprovalRepository extends JpaRepository<LeaveApproval, Long> {
    List<LeaveApproval> findByUnitId(Long unitId);
    List<LeaveApproval> findByLeaveApplicationId(Long leaveApplicationId);
}
