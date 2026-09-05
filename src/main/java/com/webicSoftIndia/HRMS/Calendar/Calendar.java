package com.webicSoftIndia.HRMS.Calendar;

import com.webicSoftIndia.HRMS.Organization.Organization;
import com.webicSoftIndia.HRMS.Unit.Unit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "calendars",
        indexes = {
                @Index(name = "idx_cal_code", columnList = "calendar_code", unique = true),
                @Index(name = "idx_cal_org", columnList = "organization_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Calendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cal_org"))
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", foreignKey = @ForeignKey(name = "fk_cal_unit"))
    private Unit unit;

    @Column(name = "calendar_code", nullable = false, unique = true, length = 50)
    private String calendarCode;

    @Column(name = "calendar_name", nullable = false, length = 100)
    private String calendarName;

    @Column(name = "financial_year", length = 20)
    private String financialYear;

    @Column(name = "week_start_day", length = 20)
    private String weekStartDay;

    @Column(name = "working_days", length = 100)
    private String workingDays;

    @Column(name = "holiday_policy", length = 255)
    private String holidayPolicy;

    @Column(name = "leave_policy", length = 255)
    private String leavePolicy;

    @Column(name = "attendance_policy", length = 255)
    private String attendancePolicy;

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