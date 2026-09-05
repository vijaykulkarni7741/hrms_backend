package com.webicSoftIndia.HRMS.Employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeRequest {
    private String employeeCode;
    private String username;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String officialEmail;
    private String password;
    private String mobileNumber;
    private String employeeType;
    private String employmentStatus;
    private String workLocation;
    private Long organizationId;
    private Long unitId;
    private Long departmentId;
    private Long designationId;
    private Long gradeId;
    private Long roleId;
}
