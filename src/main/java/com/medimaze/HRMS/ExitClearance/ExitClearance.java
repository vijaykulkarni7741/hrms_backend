package com.medimaze.HRMS.ExitClearance;

import com.medimaze.HRMS.Employee.Employee;
import com.medimaze.HRMS.Organization.Organization;
import com.medimaze.HRMS.Resignation.Resignation;
import com.medimaze.HRMS.Unit.Unit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "exit_clearances",
        indexes = {
                @Index(name = "idx_ec_res", columnList = "resignation_id"),
                @Index(name = "idx_ec_unit", columnList = "unit_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExitClearance {

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
    @JoinColumn(name = "resignation_id", nullable = false)
    private Resignation resignation;

    @Column(name = "department_name", nullable = false, length = 100)
    private String departmentName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleared_by_id")
    private Employee clearedBy;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "clearance_status", nullable = false, length = 30)
    private String clearanceStatus;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "cleared_at")
    private LocalDateTime clearedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
