package com.webicSoftIndia.HRMS.AttendancePhoto;

import com.webicSoftIndia.HRMS.Attendance.Attendance;
import com.webicSoftIndia.HRMS.AttendanceLog.AttendanceLog;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "attendance_photos",
        indexes = {
                @Index(name = "idx_attphoto_att", columnList = "attendance_id"),
                @Index(name = "idx_attphoto_log", columnList = "attendance_log_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendancePhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", foreignKey = @ForeignKey(name = "fk_attphoto_attendance"))
    private Attendance attendance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_log_id", foreignKey = @ForeignKey(name = "fk_attphoto_log"))
    private AttendanceLog attendanceLog;

    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;

    @Column(name = "captured_at", nullable = false)
    private LocalDateTime capturedAt;

    @Column(name = "verification_score")
    private Double verificationScore;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false, nullable = false)
    private LocalDateTime createdDate;
}
