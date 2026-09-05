package com.webicSoftIndia.HRMS.LeaveBalance;

import com.webicSoftIndia.HRMS.Employee.Employee;
import com.webicSoftIndia.HRMS.LeaveType.LeaveType;
import com.webicSoftIndia.HRMS.Organization.Organization;
import com.webicSoftIndia.HRMS.Unit.Unit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "leave_balances",
        indexes = {
                @Index(name = "idx_lb_emp", columnList = "employee_id"),
                @Index(name = "idx_lb_unit", columnList = "unit_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "allocated_days", precision = 5, scale = 2)
    private BigDecimal allocatedDays;

    @Column(name = "used_days", precision = 5, scale = 2)
    private BigDecimal usedDays;

    @Column(name = "remaining_days", precision = 5, scale = 2)
    private BigDecimal remainingDays;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_by", updatable = false, length = 50)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false, nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
