package com.webicSoftIndia.HRMS.EmployeeEducation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.webicSoftIndia.HRMS.Employee.Employee;

@Entity
@Table(
        name = "employee_educations",
        indexes = {
                @Index(name = "idx_emp_edu_emp_id", columnList = "employee_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_emp_edu_employee"))
    private Employee employee;

    @Column(name = "qualification", length = 100)
    private String qualification;

    @Column(name = "course_name", length = 150)
    private String courseName;

    @Column(name = "specialization", length = 150)
    private String specialization;

    @Column(name = "board_university", length = 200)
    private String boardUniversity;

    @Column(name = "college_name", length = 200)
    private String collegeName;

    @Column(name = "passing_year")
    private Integer passingYear;

    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "cgpa", precision = 4, scale = 2)
    private BigDecimal cgpa;

    @Column(name = "certificate_number", length = 100)
    private String certificateNumber;

    @Column(name = "certificate_file", length = 500)
    private String certificateFile;

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