package com.webicSoftIndia.HRMS.Roster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRosterRepository extends JpaRepository<EmployeeRoster, Long> {

    List<EmployeeRoster> findByEmployeeIdOrderByStartDateDesc(Long employeeId);

    List<EmployeeRoster> findByIsActiveTrueOrderByStartDateDesc();

    @Query("SELECT r FROM EmployeeRoster r WHERE r.employee.id = :employeeId AND r.isActive = true AND r.startDate <= :targetDate AND r.endDate >= :targetDate ORDER BY r.startDate DESC")
    List<EmployeeRoster> findExactRosterForDate(@Param("employeeId") Long employeeId, @Param("targetDate") LocalDate targetDate);

    @Query("SELECT r FROM EmployeeRoster r WHERE r.employee.id = :employeeId AND r.isActive = true AND r.isAutoRollover = true AND r.startDate <= :targetDate ORDER BY r.startDate DESC")
    List<EmployeeRoster> findAutoRolloverRostersPriorToDate(@Param("employeeId") Long employeeId, @Param("targetDate") LocalDate targetDate);
}
