package com.medimaze.HRMS.AttendanceBreak;

import com.medimaze.HRMS.Attendance.Attendance;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "attendance_breaks",
        indexes = {
                @Index(name = "idx_attbreak_att", columnList = "attendance_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceBreak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attbreak_attendance"))
    private Attendance attendance;

    @Column(name = "break_start_time", nullable = false)
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    private LocalTime breakEndTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "break_type", length = 50)
    private String breakType;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false, nullable = false)
    private LocalDateTime createdDate;
}
