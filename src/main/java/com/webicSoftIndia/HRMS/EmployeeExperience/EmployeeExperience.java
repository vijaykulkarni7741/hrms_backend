package com.webicSoftIndia.HRMS.EmployeeExperience;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.webicSoftIndia.HRMS.Employee.Employee;

@Entity
@Table(
        name = "employee_experiences",
        indexes = {
                @Index(name = "idx_emp_exp_emp_id", columnList = "employee_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_emp_exp_employee"))
    private Employee employee;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "designation", length = 100)
    private String designation;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "employment_type", length = 50)
    private String employmentType;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(name = "relieving_date")
    private LocalDate relievingDate;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "experience_months")
    private Integer experienceMonths;

    @Column(name = "salary", precision = 12, scale = 2)
    private BigDecimal salary;

    @Column(name = "manager_name", length = 100)
    private String managerName;

    @Column(name = "manager_contact", length = 50)
    private String managerContact;

    @Column(name = "reason_for_leaving", length = 255)
    private String reasonForLeaving;

    @Column(name = "experience_certificate", length = 500)
    private String experienceCertificate;

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