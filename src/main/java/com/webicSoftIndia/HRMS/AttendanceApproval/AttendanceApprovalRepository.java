package com.webicSoftIndia.HRMS.AttendanceApproval;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceApprovalRepository extends JpaRepository<AttendanceApproval, Long> {
    List<AttendanceApproval> findByApproverId(Long approverId);
}
