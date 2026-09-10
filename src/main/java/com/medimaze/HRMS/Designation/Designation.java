package com.medimaze.HRMS.Designation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import com.medimaze.HRMS.Organization.Organization;
import com.medimaze.HRMS.Grade.Grade;

@Entity
@Table(
        name = "designations",
        indexes = {
                @Index(name = "idx_desig_code", columnList = "designation_code", unique = true),
                @Index(name = "idx_desig_org", columnList = "organization_id"),
                @Index(name = "idx_desig_grade", columnList = "grade_id"),
                @Index(name = "idx_desig_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Designation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_desig_organization"))
    private Organization organization;

    @Column(name = "designation_code", nullable = false, unique = true, length = 50)
    private String designationCode;

    @Column(name = "designation_name", nullable = false, length = 150)
    private String designationName;

    @Column(name = "designation_level", length = 50)
    private String designationLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id", foreignKey = @ForeignKey(name = "fk_desig_grade"))
    private Grade grade;

    @Column(name = "description", length = 500)
    private String description;

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

    @Version
    @Column(name = "version")
    private Long version;
}