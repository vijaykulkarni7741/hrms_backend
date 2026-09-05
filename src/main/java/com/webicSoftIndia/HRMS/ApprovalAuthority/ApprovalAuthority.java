package com.webicSoftIndia.HRMS.ApprovalAuthority;

import com.webicSoftIndia.HRMS.Employee.Employee;
import com.webicSoftIndia.HRMS.Organization.Organization;
import com.webicSoftIndia.HRMS.Unit.Unit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "approval_authorities",
        indexes = {
                @Index(name = "idx_aa_emp", columnList = "employee_id"),
                @Index(name = "idx_aa_unit", columnList = "unit_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalAuthority {

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

    @Column(name = "authority_level", nullable = false)
    private Integer authorityLevel;

    @Column(name = "module_type", nullable = false, length = 50)
    private String moduleType;

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
