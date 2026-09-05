package com.webicSoftIndia.HRMS.AuditLog;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "security_audit_logs",
        indexes = {
                @Index(name = "idx_al_username", columnList = "username"),
                @Index(name = "idx_al_module", columnList = "module_name"),
                @Index(name = "idx_al_action", columnList = "action")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "module_name", nullable = false, length = 100)
    private String moduleName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "old_data", columnDefinition = "TEXT")
    private String oldData;

    @Column(name = "new_data", columnDefinition = "TEXT")
    private String newData;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false, nullable = false)
    private LocalDateTime createdDate;
}
