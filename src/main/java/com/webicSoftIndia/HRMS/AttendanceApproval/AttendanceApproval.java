package com.webicSoftIndia.HRMS.AttendanceApproval;

import com.webicSoftIndia.HRMS.Employee.Employee;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "attendance_approvals",
        indexes = {
                @Index(name = "idx_attapp_ref", columnList = "reference_type, reference_id"),
                @Index(name = "idx_attapp_approver", columnList = "approver_id"),
                @Index(name = "idx_attapp_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "reference_type", nullable = false, length = 50)
    private String referenceType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attapp_approver"))
    private Employee approver;

    @Column(name = "approval_level")
    private Integer approvalLevel;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "comments", length = 500)
    private String comments;

    @Column(name = "action_date")
    private LocalDateTime actionDate;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false, nullable = false)
    private LocalDateTime createdDate;
}
