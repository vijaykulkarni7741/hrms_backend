package com.webicSoftIndia.HRMS.NotificationHistory;

import com.webicSoftIndia.HRMS.Notification.Notification;
import com.webicSoftIndia.HRMS.Organization.Organization;
import com.webicSoftIndia.HRMS.Unit.Unit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notification_histories",
        indexes = {
                @Index(name = "idx_nth_notification", columnList = "notification_id"),
                @Index(name = "idx_nth_unit", columnList = "unit_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "sent_to", nullable = false, length = 200)
    private String sentTo;

    @Column(name = "channel", length = 30)
    private String channel;

    @Column(name = "delivery_status", length = 50)
    private String deliveryStatus;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "sent_at", updatable = false, nullable = false)
    private LocalDateTime sentAt;
}
