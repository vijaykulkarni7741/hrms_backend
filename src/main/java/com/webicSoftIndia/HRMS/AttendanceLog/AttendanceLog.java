package com.webicSoftIndia.HRMS.AttendanceLog;

import com.webicSoftIndia.HRMS.Employee.Employee;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "attendance_logs",
        indexes = {
                @Index(name = "idx_attlog_emp", columnList = "employee_id"),
                @Index(name = "idx_attlog_time", columnList = "punch_time")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attlog_employee"))
    private Employee employee;

    @Column(name = "punch_time", nullable = false)
    private LocalDateTime punchTime;

    @Column(name = "punch_type", nullable = false, length = 20)
    private String punchType;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location_name", length = 255)
    private String locationName;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false, nullable = false)
    private LocalDateTime createdDate;
}
