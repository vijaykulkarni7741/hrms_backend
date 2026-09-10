package com.medimaze.HRMS.Organization;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDate;
import java.time.LocalDateTime;

import com.medimaze.HRMS.City.City;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
        name = "organizations",
        indexes = {
                @Index(name = "idx_org_code", columnList = "organizationCode", unique = true),
                @Index(name = "idx_org_city", columnList = "city_id"),
                @Index(name = "idx_org_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "organization_code", nullable = false, unique = true, length = 50)
    private String organizationCode;

    @Column(name = "organization_name", nullable = false, length = 150)
    private String organizationName;

    @Column(name = "organization_short_name", length = 50)
    private String organizationShortName;

    @Column(name = "organization_type", length = 50)
    private String organizationType;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "gst_number", length = 15)
    private String gstNumber;

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "tan_number", length = 10)
    private String tanNumber;

    @Column(name = "company_logo", length = 500)
    private String companyLogo;

    @Column(name = "website", length = 150)
    private String website;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    @Column(name = "alternate_mobile", length = 20)
    private String alternateMobile;

    @Column(name = "landline_number", length = 20)
    private String landlineNumber;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = true)
    private City city;

    @Column(name = "pin_code", length = 10)
    private String pinCode;

    @Column(name = "time_zone", length = 50)
    private String timeZone;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "financial_year_start")
    private LocalDate financialYearStart;

    @Column(name = "financial_year_end")
    private LocalDate financialYearEnd;

    @Column(name = "working_days", length = 100)
    private String workingDays;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

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