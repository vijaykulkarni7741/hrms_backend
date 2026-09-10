package com.medimaze.HRMS.Resignation;

import com.medimaze.HRMS.Employee.Employee;
import com.medimaze.HRMS.Organization.Organization;
import com.medimaze.HRMS.Unit.Unit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "resignations",
        indexes = {
                @Index(name = "idx_res_emp", columnList = "employee_id"),
                @Index(name = "idx_res_unit", columnList = "unit_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resignation {

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
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "resignation_date", nullable = false)
    private LocalDate resignationDate;

    @Column(name = "notice_period_days")
    private Integer noticePeriodDays;

    @Column(name = "last_working_day")
    private LocalDate lastWorkingDay;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Builder.Default
    @Column(name = "requires_work_manager_approval")
    private Boolean requiresWorkManagerApproval = true;

    @Column(name = "work_manager_status", length = 30)
    private String workManagerStatus;

    @Column(name = "reporting_manager_status", length = 30)
    private String reportingManagerStatus;

    @Column(name = "hr_status", length = 30)
    private String hrStatus;

    @Column(name = "comments", length = 500)
    private String comments;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
