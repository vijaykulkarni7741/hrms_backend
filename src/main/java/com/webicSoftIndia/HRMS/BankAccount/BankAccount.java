package com.webicSoftIndia.HRMS.BankAccount;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.webicSoftIndia.HRMS.Employee.Employee;
import com.webicSoftIndia.HRMS.Bank.Bank;

@Entity
@Table(
        name = "bank_accounts",
        indexes = {
                @Index(name = "idx_ba_emp", columnList = "employee_id"),
                @Index(name = "idx_ba_acc_num", columnList = "account_number"),
                @Index(name = "idx_ba_bank", columnList = "bank_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ba_employee"))
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ba_bank"))
    private Bank bank;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "account_holder_name", nullable = false, length = 150)
    private String accountHolderName;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "account_type", length = 30)
    private String accountType;

    @Column(name = "ifsc_code", nullable = false, length = 20)
    private String ifscCode;

    @Column(name = "micr_code", length = 20)
    private String micrCode;

    @Column(name = "upi_id", length = 100)
    private String upiId;

    @Column(name = "salary_account")
    private Boolean salaryAccount;

    @Column(name = "primary_account")
    private Boolean primaryAccount;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "verification_status", length = 30)
    private String verificationStatus;

    @Column(name = "verified_by", length = 50)
    private String verifiedBy;

    @Column(name = "verified_date")
    private LocalDateTime verifiedDate;

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

    @Version
    @Column(name = "version")
    private Long version;
}