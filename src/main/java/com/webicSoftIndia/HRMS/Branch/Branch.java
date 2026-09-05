package com.webicSoftIndia.HRMS.Branch;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import com.webicSoftIndia.HRMS.Organization.Organization;
import com.webicSoftIndia.HRMS.Unit.Unit;
import com.webicSoftIndia.HRMS.City.City;

@Entity
@Table(
        name = "branches",
        indexes = {
                @Index(name = "idx_branch_code", columnList = "branch_code", unique = true),
                @Index(name = "idx_branch_org", columnList = "organization_id"),
                @Index(name = "idx_branch_unit", columnList = "unit_id"),
                @Index(name = "idx_branch_city", columnList = "city_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_branch_org"))
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", foreignKey = @ForeignKey(name = "fk_branch_unit"))
    private Unit unit;

    @Column(name = "branch_code", nullable = false, unique = true, length = 50)
    private String branchCode;

    @Column(name = "branch_name", nullable = false, length = 150)
    private String branchName;

    @Column(name = "branch_type", length = 50)
    private String branchType;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "country_id")
    private Long countryId;

    @Column(name = "state_id")
    private Long stateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", foreignKey = @ForeignKey(name = "fk_branch_city"))
    private City city;

    @Column(name = "pin_code", length = 10)
    private String pinCode;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(name = "email", length = 100)
    private String email;

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
