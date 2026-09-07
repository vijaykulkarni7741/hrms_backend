package com.webicSoftIndia.HRMS.AttendanceApproval;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface AttendanceApprovalRepository extends JpaRepository<AttendanceApproval, Long> {
    List<AttendanceApproval> findByApproverId(Long approverId);
    Optional<AttendanceApproval> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
    List<AttendanceApproval> findByReferenceTypeAndReferenceIdOrderByCreatedDateDesc(String referenceType, Long referenceId);
}
