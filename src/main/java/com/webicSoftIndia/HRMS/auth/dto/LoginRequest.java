package com.webicSoftIndia.HRMS.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginRequest {

    @JsonAlias({"employee_id", "employeeCode", "employee_code", "username"})
    private String employeeId;

    private String password;

    public String getEmployeeIdentifier() {
        if (employeeId != null && !employeeId.trim().isEmpty() && !"string".equalsIgnoreCase(employeeId.trim())) {
            return employeeId.trim();
        }
        return employeeId;
    }

    public void setEmployeeIdentifier(String employeeIdentifier) {
        if ((this.employeeId == null || this.employeeId.trim().isEmpty() || "string".equalsIgnoreCase(this.employeeId.trim()))
                && employeeIdentifier != null && !"string".equalsIgnoreCase(employeeIdentifier.trim())) {
            this.employeeId = employeeIdentifier.trim();
        }
    }
}

