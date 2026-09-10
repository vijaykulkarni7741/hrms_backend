package com.medimaze.HRMS.Notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUnitId(Long unitId);
    List<Notification> findByRecipientId(Long recipientId);
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    List<Notification> findByScopeOrderByCreatedAtDesc(String scope);
    List<Notification> findByRecipientIdOrScopeOrderByCreatedAtDesc(Long recipientId, String scope);

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId OR (n.scope = 'BROADCAST' AND (n.unit.id = :unitId OR n.unit IS NULL)) ORDER BY n.createdAt DESC")
    List<Notification> findUserNotificationsByUnit(
            @Param("recipientId") Long recipientId,
            @Param("unitId") Long unitId);
}
