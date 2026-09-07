package com.webicSoftIndia.HRMS.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantBootstrapRequest {
    private String organizationName;
    private String unitName;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
}
