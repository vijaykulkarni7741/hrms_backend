package com.medimaze.HRMS.AttendancePhoto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendancePhotoRepository extends JpaRepository<AttendancePhoto, Long> {
    List<AttendancePhoto> findByAttendanceId(Long attendanceId);
}
