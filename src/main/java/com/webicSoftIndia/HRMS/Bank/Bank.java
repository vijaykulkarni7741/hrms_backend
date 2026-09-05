package com.webicSoftIndia.HRMS.Bank;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "banks",
        indexes = {
                @Index(name = "idx_bank_code", columnList = "bank_code", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "bank_code", nullable = false, unique = true, length = 50)
    private String bankCode;

    @Column(name = "bank_name", nullable = false, length = 150)
    private String bankName;

    @Column(name = "short_name", length = 50)
    private String shortName;

    @Column(name = "ifsc_prefix", length = 10)
    private String ifscPrefix;

    @Column(name = "swift_code", length = 20)
    private String swiftCode;

    @Column(name = "website", length = 150)
    private String website;

    @Column(name = "customer_care_number", length = 30)
    private String customerCareNumber;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

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