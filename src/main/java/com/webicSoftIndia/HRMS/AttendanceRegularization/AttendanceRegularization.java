package com.webicSoftIndia.HRMS.AttendanceRegularization;

import com.webicSoftIndia.HRMS.Attendance.Attendance;
import com.webicSoftIndia.HRMS.Employee.Employee;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "attendance_regularizations",
        indexes = {
                @Index(name = "idx_reg_emp", columnList = "employee_id"),
                @Index(name = "idx_reg_att", columnList = "attendance_id"),
                @Index(name = "idx_reg_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRegularization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", foreignKey = @ForeignKey(name = "fk_reg_attendance"))
    private Attendance attendance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reg_employee"))
    private Employee employee;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Column(name = "regularization_date", nullable = false)
    private LocalDate regularizationDate;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "requested_in_time")
    private LocalTime requestedInTime;

    @Column(name = "requested_out_time")
    private LocalTime requestedOutTime;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", foreignKey = @ForeignKey(name = "fk_reg_approver"))
    private Employee approvedBy;

    @Column(name = "approval_remarks", length = 500)
    private String approvalRemarks;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false, nullable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
