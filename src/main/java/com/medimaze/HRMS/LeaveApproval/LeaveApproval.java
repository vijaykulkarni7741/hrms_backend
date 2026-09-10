package com.medimaze.HRMS.LeaveApproval;

import com.medimaze.HRMS.Employee.Employee;
import com.medimaze.HRMS.LeaveApplication.LeaveApplication;
import com.medimaze.HRMS.Organization.Organization;
import com.medimaze.HRMS.Unit.Unit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "leave_approvals",
        indexes = {
                @Index(name = "idx_lap_la", columnList = "leave_application_id"),
                @Index(name = "idx_lap_approver", columnList = "approver_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveApproval {

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
    @JoinColumn(name = "leave_application_id", nullable = false)
    private LeaveApplication leaveApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private Employee approver;

    @Column(name = "action", nullable = false, length = 30)
    private String action;

    @Column(name = "comments", length = 500)
    private String comments;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "action_date", updatable = false, nullable = false)
    private LocalDateTime actionDate;
}
