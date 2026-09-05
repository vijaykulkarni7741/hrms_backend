package com.webicSoftIndia.HRMS.Attendance;

import com.webicSoftIndia.HRMS.Employee.Employee;
import com.webicSoftIndia.HRMS.Shift.Shift;
import com.webicSoftIndia.HRMS.Unit.Unit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "attendance_records",
        indexes = {
                @Index(name = "idx_att_emp", columnList = "employee_id"),
                @Index(name = "idx_att_unit", columnList = "unit_id"),
                @Index(name = "idx_att_date", columnList = "attendance_date"),
                @Index(name = "idx_att_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_att_employee"))
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", foreignKey = @ForeignKey(name = "fk_att_unit"))
    private Unit unit;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", foreignKey = @ForeignKey(name = "fk_att_shift"))
    private Shift shift;

    @Column(name = "in_time")
    private LocalTime inTime;

    @Column(name = "out_time")
    private LocalTime outTime;

    @Column(name = "total_hours", precision = 4, scale = 2)
    private BigDecimal totalHours;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Builder.Default
    @Column(name = "is_late")
    private Boolean isLate = false;

    @Builder.Default
    @Column(name = "is_early_departure")
    private Boolean isEarlyDeparture = false;

    @Column(name = "overtime_hours", precision = 4, scale = 2)
    private BigDecimal overtimeHours;

    @Column(name = "remarks", length = 500)
    private String remarks;

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
