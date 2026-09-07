package com.webicSoftIndia.HRMS.LeaveApplication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {
    List<LeaveApplication> findByUnitId(Long unitId);
    List<LeaveApplication> findByEmployeeId(Long employeeId);
    List<LeaveApplication> findByEmployeeIdOrderByCreatedDateDesc(Long employeeId);
    List<LeaveApplication> findByReportingManagerIdOrWorkingManagerId(Long reportingManagerId, Long workingManagerId);

    @Query("SELECT COUNT(la) FROM LeaveApplication la WHERE la.employee.id = :empId AND la.startDate BETWEEN :startDate AND :endDate AND la.isActive = true")
    long countLeaveApplicationsInMonth(@Param("empId") Long empId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
