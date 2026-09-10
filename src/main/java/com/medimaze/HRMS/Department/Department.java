package com.medimaze.HRMS.Department;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import com.medimaze.HRMS.Organization.Organization;
import com.medimaze.HRMS.Unit.Unit;

@Entity
@Table(
        name = "departments",
        indexes = {
                @Index(name = "idx_dept_code", columnList = "department_code", unique = true),
                @Index(name = "idx_dept_org", columnList = "organization_id"),
                @Index(name = "idx_dept_unit", columnList = "unit_id"),
                @Index(name = "idx_dept_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "organization_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_departments_organization")
    )
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "unit_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_departments_unit")
    )
    private Unit unit;

    @Column(name = "department_code", nullable = false, unique = true, length = 50)
    private String departmentCode;

    @Column(name = "department_name", nullable = false, length = 150)
    private String departmentName;

    @Column(name = "department_type", length = 50)
    private String departmentType;

    @Column(name = "department_head_employee_id")
    private Long departmentHeadEmployeeId;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "mobile", length = 20)
    private String mobile;

    @Column(name = "extension_number", length = 20)
    private String extensionNumber;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder;

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

    @Version
    @Column(name = "version")
    private Long version;
}
