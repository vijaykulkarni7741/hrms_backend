package com.webicSoftIndia.HRMS.Notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNotificationReadRepository extends JpaRepository<UserNotificationRead, Long> {
    List<UserNotificationRead> findByEmployeeId(Long employeeId);
    boolean existsByNotificationIdAndEmployeeId(Long notificationId, Long employeeId);
}
