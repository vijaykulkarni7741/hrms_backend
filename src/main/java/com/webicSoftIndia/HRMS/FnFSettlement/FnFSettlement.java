package com.webicSoftIndia.HRMS.FnFSettlement;

import com.webicSoftIndia.HRMS.Employee.Employee;
import com.webicSoftIndia.HRMS.Organization.Organization;
import com.webicSoftIndia.HRMS.Resignation.Resignation;
import com.webicSoftIndia.HRMS.Unit.Unit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "fnf_settlements",
        indexes = {
                @Index(name = "idx_fnf_emp", columnList = "employee_id"),
                @Index(name = "idx_fnf_res", columnList = "resignation_id"),
                @Index(name = "idx_fnf_unit", columnList = "unit_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FnFSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resignation_id", nullable = false)
    private Resignation resignation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "basic_pay_amount", precision = 12, scale = 2)
    private BigDecimal basicPayAmount;

    @Column(name = "leave_encashment_amount", precision = 12, scale = 2)
    private BigDecimal leaveEncashmentAmount;

    @Column(name = "gratuity_amount", precision = 12, scale = 2)
    private BigDecimal gratuityAmount;

    @Column(name = "deductions_amount", precision = 12, scale = 2)
    private BigDecimal deductionsAmount;

    @Column(name = "net_payable_amount", precision = 12, scale = 2)
    private BigDecimal netPayableAmount;

    @Column(name = "settlement_date")
    private LocalDate settlementDate;

    @Column(name = "payment_mode", length = 50)
    private String paymentMode;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;
}
