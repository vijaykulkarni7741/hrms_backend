package com.medimaze.HRMS.Unit;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import com.medimaze.HRMS.Organization.Organization;
import com.medimaze.HRMS.City.City;

@Entity
@Table(
        name = "units",
        indexes = {
                @Index(name = "idx_unit_code", columnList = "unit_code", unique = true),
                @Index(name = "idx_unit_org", columnList = "organization_id"),
                @Index(name = "idx_unit_city", columnList = "city_id"),
                @Index(name = "idx_unit_hospital_code", columnList = "hospital_code"),
                @Index(name = "idx_unit_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_unit_organization"))
    private Organization organization;

    @Column(name = "unit_code", nullable = false, unique = true, length = 50)
    private String unitCode;

    @Column(name = "unit_name", nullable = false, length = 150)
    private String unitName;

    @Column(name = "unit_type", length = 50)
    private String unitType;

    @Column(name = "hospital_code", length = 50)
    private String hospitalCode;

    @Column(name = "license_number", length = 100)
    private String licenseNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    @Column(name = "landline_number", length = 20)
    private String landlineNumber;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", foreignKey = @ForeignKey(name = "fk_unit_city"))
    private City city;

    @Column(name = "pin_code", length = 10)
    private String pinCode;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "allowed_radius")
    private Double allowedRadius;

    @Column(name = "time_zone", length = 50)
    private String timeZone;

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