package com.webicSoftIndia.HRMS.Attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByUnitId(Long unitId);
    List<Attendance> findByEmployeeId(Long employeeId);
    List<Attendance> findByEmployeeIdOrderByAttendanceDateDesc(Long employeeId);
    List<Attendance> findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(Long employeeId, LocalDate startDate, LocalDate endDate);
    Optional<Attendance> findTopByEmployeeIdAndAttendanceDateOrderByCreatedDateDesc(Long employeeId, LocalDate attendanceDate);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :empId AND a.attendanceDate BETWEEN :startDate AND :endDate AND (UPPER(a.status) LIKE '%REGULARIZ%' OR a.status = 'PENDING_REGULARIZATION' OR a.status = 'REGULARIZED') AND (:excludeId IS NULL OR a.id <> :excludeId)")
    long countRegularizationsInMonth(@Param("empId") Long empId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("excludeId") Long excludeId);
}
