package com.webicSoftIndia.HRMS.Employee;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.webicSoftIndia.HRMS.Organization.Organization;
import com.webicSoftIndia.HRMS.Unit.Unit;
import com.webicSoftIndia.HRMS.Department.Department;
import com.webicSoftIndia.HRMS.Designation.Designation;
import com.webicSoftIndia.HRMS.Grade.Grade;
import com.webicSoftIndia.HRMS.Role.Role;

@Entity
@Table(name = "employees", indexes = {
                @Index(name = "idx_emp_code", columnList = "employee_code", unique = true),
                @Index(name = "idx_emp_org", columnList = "organization_id"),
                @Index(name = "idx_emp_unit", columnList = "unit_id"),
                @Index(name = "idx_emp_dept", columnList = "department_id"),
                @Index(name = "idx_emp_desig", columnList = "designation_id"),
                @Index(name = "idx_emp_pan", columnList = "pan_number"),
                @Index(name = "idx_emp_biometric", columnList = "biometric_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", updatable = false, nullable = false)
        private Long id;

        @Column(name = "employee_code", nullable = false, unique = true, length = 50)
        private String employeeCode;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_emp_organization"))
        private Organization organization;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "unit_id", nullable = false, foreignKey = @ForeignKey(name = "fk_emp_unit"))
        private Unit unit;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_emp_department"))
        private Department department;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "designation_id", foreignKey = @ForeignKey(name = "fk_emp_designation"))
        private Designation designation;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "grade_id", foreignKey = @ForeignKey(name = "fk_emp_grade"))
        private Grade grade;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_emp_role"))
        private Role role;

        @Column(name = "cost_center_id")
        private Long costCenterId;

        @Column(name = "branch_id")
        private Long branchId;

        @Column(name = "manager_id")
        private Long managerId;

        @Column(name = "reporting_manager_id")
        private Long reportingManagerId;

        @Column(name = "working_manager_id")
        private Long workingManagerId;

        @Column(name = "employee_type", length = 50)
        private String employeeType;

        @Column(name = "employment_status", length = 50)
        private String employmentStatus;

        @Column(name = "joining_date")
        private LocalDate joiningDate;

        @Column(name = "confirmation_date")
        private LocalDate confirmationDate;

        @Column(name = "exit_date")
        private LocalDate exitDate;

        @Column(name = "title", length = 20)
        private String title;

        @Column(name = "first_name", nullable = false, length = 100)
        private String firstName;

        @Column(name = "middle_name", length = 100)
        private String middleName;

        @Column(name = "last_name", nullable = false, length = 100)
        private String lastName;

        @Column(name = "gender", length = 20)
        private String gender;

        @Column(name = "date_of_birth")
        private LocalDate dateOfBirth;

        @Column(name = "blood_group", length = 10)
        private String bloodGroup;

        @Column(name = "marital_status", length = 20)
        private String maritalStatus;

        @Column(name = "nationality", length = 50)
        private String nationality;

        @Column(name = "religion", length = 50)
        private String religion;

        @Column(name = "aadhaar_number", length = 20)
        private String aadhaarNumber;

        @Column(name = "pan_number", length = 10)
        private String panNumber;

        @Column(name = "passport_number", length = 30)
        private String passportNumber;

        @Column(name = "driving_license_number", length = 50)
        private String drivingLicenseNumber;

        @Column(name = "email", length = 100)
        private String email;

        @Column(name = "official_email", length = 100)
        private String officialEmail;

        @Column(name = "mobile_number", length = 20)
        private String mobileNumber;

        @Column(name = "alternate_mobile", length = 20)
        private String alternateMobile;

        @Column(name = "emergency_contact_name", length = 100)
        private String emergencyContactName;

        @Column(name = "emergency_contact_number", length = 20)
        private String emergencyContactNumber;

        @Column(name = "profile_photo", length = 500)
        private String profilePhoto;

        @Column(name = "signature_image", length = 500)
        private String signatureImage;

        @Column(name = "biometric_id", length = 50)
        private String biometricId;

        @Column(name = "shift_id")
        private Long shiftId;

        @Column(name = "holiday_calendar_id")
        private Long holidayCalendarId;

        @Column(name = "bank_account_id")
        private Long bankAccountId;

        @Column(name = "pf_number", length = 50)
        private String pfNumber;

        @Column(name = "esi_number", length = 50)
        private String esiNumber;

        @Column(name = "uan_number", length = 50)
        private String uanNumber;

        @Column(name = "salary_structure_id")
        private Long salaryStructureId;

        @Column(name = "notice_period")
        private Integer noticePeriod;

        @Column(name = "probation_period")
        private Integer probationPeriod;

        @Column(name = "work_location", length = 100)
        private String workLocation;

        @Column(name = "week_off_day_1", length = 20)
        private String weekOffDay1;

        @Column(name = "week_off_day_2", length = 20)
        private String weekOffDay2;

        @Builder.Default
        @Column(name = "is_active", nullable = false)
        private Boolean isActive = true;

        @Column(name = "remarks", length = 500)
        private String remarks;

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

        @Version
        @Column(name = "version")
        private Long version;
}