package com.medimaze.HRMS.Shift;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.medimaze.HRMS.Organization.Organization;
import com.medimaze.HRMS.Unit.Unit;

@Entity
@Table(
        name = "shifts",
        indexes = {
                @Index(name = "idx_shift_code", columnList = "shift_code", unique = true),
                @Index(name = "idx_shift_org", columnList = "organization_id"),
                @Index(name = "idx_shift_unit", columnList = "unit_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_shift_org"))
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", foreignKey = @ForeignKey(name = "fk_shift_unit"))
    private Unit unit;

    @Column(name = "shift_code", nullable = false, unique = true, length = 50)
    private String shiftCode;

    @Column(name = "shift_name", nullable = false, length = 100)
    private String shiftName;

    @Column(name = "shift_type", length = 50)
    private String shiftType;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "break_start_time")
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    private LocalTime breakEndTime;

    @Column(name = "working_hours", precision = 4, scale = 2)
    private BigDecimal workingHours;

    @Column(name = "minimum_working_hours", precision = 4, scale = 2)
    private BigDecimal minimumWorkingHours;

    @Column(name = "grace_time")
    private Integer graceTime;

    @Column(name = "late_mark_after")
    private Integer lateMarkAfter;

    @Column(name = "half_day_hours", precision = 4, scale = 2)
    private BigDecimal halfDayHours;

    @Column(name = "night_shift")
    private Boolean nightShift;

    @Column(name = "cross_day_shift")
    private Boolean crossDayShift;

    @Column(name = "overtime_allowed")
    private Boolean overtimeAllowed;

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