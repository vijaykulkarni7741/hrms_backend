package com.webicSoftIndia.HRMS.EmployeeAddress;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

import com.webicSoftIndia.HRMS.Employee.Employee;
import com.webicSoftIndia.HRMS.City.City;

@Entity
@Table(
        name = "employee_addresses",
        indexes = {
                @Index(name = "idx_emp_addr_emp_id", columnList = "employee_id"),
                @Index(name = "idx_emp_addr_city_id", columnList = "city_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_emp_addr_employee"))
    private Employee employee;

    @Column(name = "address_type", length = 30)
    private String addressType;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "landmark", length = 100)
    private String landmark;

    @Column(name = "country_id")
    private Long countryId;

    @Column(name = "state_id")
    private Long stateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", foreignKey = @ForeignKey(name = "fk_emp_addr_city"))
    private City city;

    @Column(name = "pin_code", length = 10)
    private String pinCode;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "is_current_address")
    private Boolean isCurrentAddress;

    @Column(name = "is_permanent_address")
    private Boolean isPermanentAddress;

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