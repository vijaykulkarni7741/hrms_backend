package com.medimaze.HRMS.Holiday;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.medimaze.HRMS.Organization.Organization;
import com.medimaze.HRMS.Unit.Unit;

@Entity
@Table(
        name = "holidays",
        indexes = {
                @Index(name = "idx_holiday_code", columnList = "holiday_code", unique = true),
                @Index(name = "idx_holiday_org", columnList = "organization_id"),
                @Index(name = "idx_holiday_unit", columnList = "unit_id"),
                @Index(name = "idx_holiday_date", columnList = "holiday_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_holiday_org"))
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", foreignKey = @ForeignKey(name = "fk_holiday_unit"))
    private Unit unit;

    @Column(name = "holiday_code", nullable = false, unique = true, length = 50)
    private String holidayCode;

    @Column(name = "holiday_name", nullable = false, length = 150)
    private String holidayName;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "holiday_type", length = 50)
    private String holidayType;

    @Column(name = "is_optional")
    private Boolean isOptional;

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
}