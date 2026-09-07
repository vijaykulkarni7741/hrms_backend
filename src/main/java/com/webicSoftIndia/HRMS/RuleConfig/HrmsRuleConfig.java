package com.webicSoftIndia.HRMS.RuleConfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webicSoftIndia.HRMS.Organization.Organization;
import com.webicSoftIndia.HRMS.Unit.Unit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "hrms_rule_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class HrmsRuleConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Builder.Default
    @Column(name = "planned_leave_advance_days", nullable = false)
    private Integer plannedLeaveAdvanceDays = 5;

    @Builder.Default
    @Column(name = "regularization_max_days", nullable = false)
    private Integer regularizationMaxDays = 7;

    @Builder.Default
    @Column(name = "max_regularizations_per_month", nullable = true)
    private Integer maxRegularizationsPerMonth = 3;

    @Builder.Default
    @Column(name = "max_leave_applications_per_month", nullable = true)
    private Integer maxLeaveApplicationsPerMonth = 10;

    @Builder.Default
    @Column(name = "allow_same_day_regularization", nullable = true)
    private Boolean allowSameDayRegularization = true;

    @Builder.Default
    @Column(name = "allow_future_date_regularization", nullable = true)
    private Boolean allowFutureDateRegularization = false;

    @Builder.Default
    @Column(name = "standard_in_time", length = 10)
    private String standardInTime = "09:30";

    @Builder.Default
    @Column(name = "standard_out_time", length = 10)
    private String standardOutTime = "18:30";

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
