package com.medimaze.HRMS.LeaveAttachment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveAttachmentRepository extends JpaRepository<LeaveAttachment, Long> {
    List<LeaveAttachment> findByUnitId(Long unitId);
    List<LeaveAttachment> findByLeaveApplicationId(Long leaveApplicationId);
}
