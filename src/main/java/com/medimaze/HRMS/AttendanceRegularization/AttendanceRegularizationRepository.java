package com.medimaze.HRMS.AttendanceRegularization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRegularizationRepository extends JpaRepository<AttendanceRegularization, Long> {
    List<AttendanceRegularization> findByEmployeeId(Long employeeId);
    List<AttendanceRegularization> findByEmployeeIdOrderByCreatedDateDesc(Long employeeId);
    List<AttendanceRegularization> findByStatus(String status);
}
