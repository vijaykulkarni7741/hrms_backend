package com.medimaze.HRMS.AttendanceBreak;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceBreakRepository extends JpaRepository<AttendanceBreak, Long> {
    List<AttendanceBreak> findByAttendanceId(Long attendanceId);
}
