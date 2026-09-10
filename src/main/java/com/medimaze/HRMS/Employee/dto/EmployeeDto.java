package com.medimaze.HRMS.Employee.dto;

import com.medimaze.HRMS.Employee.Employee;
import com.medimaze.HRMS.User.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String employeeCode;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String officialEmail;
    private String mobileNumber;
    private String employeeType;
    private String employmentStatus;
    private Long organizationId;
    private String organizationName;
    private String organizationLogo;
    private Long unitId;
    private String unitName;
    private String unitLogo;
    private Long departmentId;
    private String departmentName;
    private Long designationId;
    private String designationName;
    private Long gradeId;
    private String gradeName;
    private Long roleId;
    private String roleCode;
    private String roleName;
    private Long managerId;
    private Long reportingManagerId;
    private Long workingManagerId;
    private String workLocation;
    private String weekOffDay1;
    private String weekOffDay2;
    private Integer noticePeriod;
    private Integer noticePeriodDays;
    private Boolean isActive;
    private String token;

    // User details
    private Long userId;
    private String username;
    private String userStatus;

    public static EmployeeDto fromEntity(Employee employee) {
        return fromEntity(employee, null);
    }

    public static EmployeeDto fromEntity(Employee employee, User user) {
        if (employee == null) {
            return null;
        }
        EmployeeDto dto = EmployeeDto.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .middleName(employee.getMiddleName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .officialEmail(employee.getOfficialEmail())
                .mobileNumber(employee.getMobileNumber())
                .employeeType(employee.getEmployeeType())
                .employmentStatus(employee.getEmploymentStatus())

                .organizationId(employee.getOrganization() != null ? employee.getOrganization().getId() : null)
                .organizationName(employee.getOrganization() != null ? employee.getOrganization().getOrganizationName() : null)
                .organizationLogo(employee.getOrganization() != null ? employee.getOrganization().getCompanyLogo() : null)

                .unitId(employee.getUnit() != null ? employee.getUnit().getId() : null)
                .unitName(employee.getUnit() != null ? employee.getUnit().getUnitName() : null)
                .unitLogo(employee.getOrganization() != null ? employee.getOrganization().getCompanyLogo() : null)

                .departmentId(employee.getDepartment() != null ? employee.getDepartment().getId() : null)
                .departmentName(employee.getDepartment() != null ? employee.getDepartment().getDepartmentName() : null)

                .designationId(employee.getDesignation() != null ? employee.getDesignation().getId() : null)
                .designationName(employee.getDesignation() != null ? employee.getDesignation().getDesignationName() : null)

                .gradeId(employee.getGrade() != null ? employee.getGrade().getId() : null)
                .gradeName(employee.getGrade() != null ? employee.getGrade().getGradeName() : null)

                .roleId(employee.getRole() != null ? employee.getRole().getId() : null)
                .roleCode(employee.getRole() != null ? employee.getRole().getRoleCode() : null)
                .roleName(employee.getRole() != null ? employee.getRole().getRoleName() : null)

                .managerId(employee.getManagerId())
                .reportingManagerId(employee.getReportingManagerId())
                .workingManagerId(employee.getWorkingManagerId() != null ? employee.getWorkingManagerId() : employee.getManagerId())

                .workLocation(employee.getWorkLocation())
                .weekOffDay1(employee.getWeekOffDay1())
                .weekOffDay2(employee.getWeekOffDay2())
                .noticePeriod(employee.getNoticePeriod())
                .noticePeriodDays(employee.getNoticePeriod())
                .isActive(employee.getIsActive())
                .build();

        if (user != null) {
            dto.setUserId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setUserStatus(user.getStatus());
        }

        return dto;
    }
}
