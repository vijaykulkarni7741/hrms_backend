package com.medimaze.HRMS.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantBootstrapResponse {
    private Long userId;
    private String username;
    private String employeeCode;
    private String roleCode;
    private String roleName;
    private Long organizationId;
    private String organizationName;
    private Long unitId;
    private String unitName;
    private Integer totalModulesCreated;
    private Integer totalPermissionsGranted;
    private List<String> seededModuleCodes;
}
