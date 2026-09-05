package com.webicSoftIndia.HRMS.Employee;

import com.webicSoftIndia.HRMS.Department.Department;
import com.webicSoftIndia.HRMS.Department.DepartmentRepository;
import com.webicSoftIndia.HRMS.Designation.Designation;
import com.webicSoftIndia.HRMS.Designation.DesignationRepository;
import com.webicSoftIndia.HRMS.Employee.dto.CreateEmployeeRequest;
import com.webicSoftIndia.HRMS.Grade.Grade;
import com.webicSoftIndia.HRMS.Grade.GradeRepository;
import com.webicSoftIndia.HRMS.Organization.Organization;
import com.webicSoftIndia.HRMS.Organization.OrganizationRepository;
import com.webicSoftIndia.HRMS.Role.Role;
import com.webicSoftIndia.HRMS.Role.RoleRepository;
import com.webicSoftIndia.HRMS.Unit.Unit;
import com.webicSoftIndia.HRMS.Unit.UnitRepository;
import com.webicSoftIndia.HRMS.PasswordHistory.PasswordHistoryService;
import com.webicSoftIndia.HRMS.User.User;
import com.webicSoftIndia.HRMS.User.UserRepository;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordHistoryService passwordHistoryService;
    private final OrganizationRepository organizationRepository;
    private final UnitRepository unitRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final GradeRepository gradeRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ApiResponse<Void> createEmployee(CreateEmployeeRequest request) {
        if (request.getEmployeeCode() == null || request.getEmployeeCode().trim().isEmpty()) {
            return ApiResponse.error("Employee code is required");
        }
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            return ApiResponse.error("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            return ApiResponse.error("Last name is required");
        }

        if (employeeRepository.findByEmployeeCode(request.getEmployeeCode().trim()).isPresent()) {
            return ApiResponse.error("Employee code already exists");
        }

        Organization organization = null;
        if (request.getOrganizationId() != null) {
            organization = organizationRepository.findById(request.getOrganizationId()).orElse(null);
        }

        Unit unit = null;
        if (request.getUnitId() != null) {
            unit = unitRepository.findById(request.getUnitId()).orElse(null);
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId()).orElse(null);
        }

        Designation designation = null;
        if (request.getDesignationId() != null) {
            designation = designationRepository.findById(request.getDesignationId()).orElse(null);
        }

        Grade grade = null;
        if (request.getGradeId() != null) {
            grade = gradeRepository.findById(request.getGradeId()).orElse(null);
        }

        Role role = null;
        if (request.getRoleId() != null) {
            role = roleRepository.findById(request.getRoleId()).orElse(null);
        }

        String encodedPassword = null;
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            encodedPassword = passwordEncoder.encode(request.getPassword().trim());
        }

        Employee employee = Employee.builder()
                .employeeCode(request.getEmployeeCode().trim())
                .firstName(request.getFirstName().trim())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName().trim())
                .email(request.getEmail())
                .officialEmail(request.getOfficialEmail())
                .mobileNumber(request.getMobileNumber())
                .employeeType(request.getEmployeeType())
                .employmentStatus(request.getEmploymentStatus())
                .workLocation(request.getWorkLocation())
                .organization(organization)
                .unit(unit)
                .department(department)
                .designation(designation)
                .grade(grade)
                .role(role)
                .isActive(true)
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        // Store password and user details in User entity (User & Employee unification)
        String targetUsername = (request.getUsername() != null && !request.getUsername().trim().isEmpty())
                ? request.getUsername().trim()
                : savedEmployee.getEmployeeCode();

        User user = userRepository.findByEmployeeId(savedEmployee.getId())
                .orElseGet(() -> userRepository.findByUsername(targetUsername).orElse(new User()));

        user.setUsername(targetUsername);
        boolean passwordChanged = false;
        if (encodedPassword != null) {
            user.setPassword(encodedPassword);
            passwordChanged = true;
        } else if (user.getPassword() == null) {
            // Default password encoded if none provided
            user.setPassword(passwordEncoder.encode("Password@123"));
            passwordChanged = true;
        }
        user.setEmployee(savedEmployee);
        user.setEmail(savedEmployee.getOfficialEmail() != null ? savedEmployee.getOfficialEmail() : savedEmployee.getEmail());
        user.setStatus("ACTIVE");
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        // Automatically record entry in PasswordHistory
        if (passwordChanged && savedUser.getPassword() != null) {
            passwordHistoryService.recordPasswordHistory(
                    savedUser,
                    savedUser.getPassword(),
                    savedEmployee.getCreatedBy() != null ? savedEmployee.getCreatedBy() : savedEmployee.getEmployeeCode()
            );
        }

        log.info("Successfully created employee [{}] and user [{}]", savedEmployee.getEmployeeCode(), targetUsername);

        return ApiResponse.success("Employee created successfully", null);
    }
}
