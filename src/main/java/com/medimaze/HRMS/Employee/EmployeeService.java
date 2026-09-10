package com.medimaze.HRMS.Employee;

import com.medimaze.HRMS.Notification.NotificationService;
import com.medimaze.HRMS.Department.Department;
import com.medimaze.HRMS.Department.DepartmentRepository;
import com.medimaze.HRMS.Designation.Designation;
import com.medimaze.HRMS.Designation.DesignationRepository;
import com.medimaze.HRMS.Employee.dto.CreateEmployeeRequest;
import com.medimaze.HRMS.Employee.dto.EmployeeDto;
import com.medimaze.HRMS.Grade.Grade;
import com.medimaze.HRMS.Grade.GradeRepository;
import com.medimaze.HRMS.Organization.Organization;
import com.medimaze.HRMS.Organization.OrganizationRepository;
import com.medimaze.HRMS.Role.Role;
import com.medimaze.HRMS.Role.RoleRepository;
import com.medimaze.HRMS.Unit.Unit;
import com.medimaze.HRMS.Unit.UnitRepository;
import com.medimaze.HRMS.PasswordHistory.PasswordHistoryService;
import com.medimaze.HRMS.User.User;
import com.medimaze.HRMS.User.UserRepository;
import com.medimaze.HRMS.common.ApiResponse;
import com.medimaze.HRMS.security.RedisSessionService;
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
    private final RedisSessionService redisSessionService;
    private final NotificationService notificationService;

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
                .noticePeriod(request.getNoticePeriodDays() != null ? request.getNoticePeriodDays() : (request.getNoticePeriod() != null ? request.getNoticePeriod() : 30))
                .organization(organization)
                .unit(unit)
                .department(department)
                .designation(designation)
                .grade(grade)
                .role(role)
                .isActive(true)
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        // Send broadcast notification to all employees for new team member
        try {
            notificationService.createBroadcastNotification(
                    "🎉 New Team Member Joined!",
                    "Please welcome " + savedEmployee.getFirstName() + " " + savedEmployee.getLastName() + " to our team!",
                    "NEW_JOINEE"
            );
        } catch (Exception e) {
            log.error("Failed to broadcast new joinee notification: {}", e.getMessage());
        }

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

    @Transactional(readOnly = true)
    public ApiResponse<java.util.List<Employee>> getEmployeesBySessionUnit(String sessionId) {
        java.util.List<Employee> list;
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
            if (sessionData != null && sessionData.getUnitId() != null && sessionData.getUnitId() > 0) {
                list = employeeRepository.findByUnitId(sessionData.getUnitId());
                return ApiResponse.success("Employees retrieved successfully for unit ID: " + sessionData.getUnitId(), list);
            }
        }
        list = employeeRepository.findAll();
        return ApiResponse.success("Employees retrieved successfully", list);
    }

    @Transactional(readOnly = true)
    public ApiResponse<java.util.List<Employee>> getAllEmployees() {
        return getEmployeesBySessionUnit(null);
    }

    @Transactional
    public ApiResponse<Employee> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Employee ID is required");
        }
        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee == null) {
            return ApiResponse.error("Employee not found with ID: " + id);
        }
        boolean newStatus = !Boolean.TRUE.equals(employee.getIsActive());
        employee.setIsActive(newStatus);
        Employee updated = employeeRepository.save(employee);
        return ApiResponse.success("Employee active status toggled successfully to " + newStatus, updated);
    }

    @Transactional
    public ApiResponse<Void> deleteEmployee(Long id) {
        if (id != null && employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
            return ApiResponse.success("Employee deleted successfully", null);
        }
        return ApiResponse.error("Employee not found with ID: " + id);
    }

    @Transactional
    public ApiResponse<Employee> assignShiftAndWeekOff(Long employeeId, Long shiftId, String weekOffDay1, String weekOffDay2) {
        if (employeeId == null || employeeId <= 0) {
            return ApiResponse.error("Valid Employee ID is required");
        }
        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) {
            return ApiResponse.error("Employee not found with ID: " + employeeId);
        }

        if (shiftId != null) employee.setShiftId(shiftId);
        if (weekOffDay1 != null && !weekOffDay1.trim().isEmpty()) {
            employee.setWeekOffDay1(weekOffDay1.trim().toUpperCase());
        }
        if (weekOffDay2 != null && !weekOffDay2.trim().isEmpty()) {
            employee.setWeekOffDay2(weekOffDay2.trim().toUpperCase());
        }

        Employee saved = employeeRepository.save(employee);
        log.info("Configured Shift ID {} and Week-Offs ({}, {}) for Employee ID {}", shiftId, weekOffDay1, weekOffDay2, employeeId);
        return ApiResponse.success("Shift and Week-Off configured successfully for employee " + saved.getFirstName() + " " + saved.getLastName(), saved);
    }

    @Transactional
    public ApiResponse<Employee> updateEmployeeDemographics(java.util.Map<String, Object> payload) {
        if (payload == null) return ApiResponse.error("Payload cannot be null");

        Long id = payload.get("id") != null ? Long.valueOf(payload.get("id").toString()) : null;
        String empCode = payload.get("employeeCode") != null ? payload.get("employeeCode").toString() : null;

        Employee employee = null;
        if (id != null && id > 0) {
            employee = employeeRepository.findById(id).orElse(null);
        }
        if (employee == null && empCode != null && !empCode.trim().isEmpty()) {
            employee = employeeRepository.findByEmployeeCode(empCode.trim()).orElse(null);
        }
        if (employee == null) {
            employee = employeeRepository.findAll().stream().findFirst().orElse(null);
        }
        if (employee == null) {
            return ApiResponse.error("Employee profile record not found");
        }

        if (payload.get("firstName") != null) employee.setFirstName(payload.get("firstName").toString());
        if (payload.get("middleName") != null) employee.setMiddleName(payload.get("middleName").toString());
        if (payload.get("lastName") != null) employee.setLastName(payload.get("lastName").toString());
        if (payload.get("gender") != null) employee.setGender(payload.get("gender").toString());

        if (payload.get("bloodGroup") != null) employee.setBloodGroup(payload.get("bloodGroup").toString());
        if (payload.get("maritalStatus") != null) employee.setMaritalStatus(payload.get("maritalStatus").toString());
        if (payload.get("nationality") != null) employee.setNationality(payload.get("nationality").toString());
        if (payload.get("religion") != null) employee.setReligion(payload.get("religion").toString());

        if (payload.get("mobileNumber") != null) employee.setMobileNumber(payload.get("mobileNumber").toString());
        if (payload.get("alternateMobile") != null) employee.setAlternateMobile(payload.get("alternateMobile").toString());
        if (payload.get("email") != null) employee.setEmail(payload.get("email").toString());
        if (payload.get("officialEmail") != null) employee.setOfficialEmail(payload.get("officialEmail").toString());

        if (payload.get("emergencyContactName") != null) employee.setEmergencyContactName(payload.get("emergencyContactName").toString());
        if (payload.get("emergencyContactNumber") != null) employee.setEmergencyContactNumber(payload.get("emergencyContactNumber").toString());

        if (payload.get("aadhaarNumber") != null) employee.setAadhaarNumber(payload.get("aadhaarNumber").toString());
        if (payload.get("panNumber") != null) employee.setPanNumber(payload.get("panNumber").toString());
        if (payload.get("passportNumber") != null) employee.setPassportNumber(payload.get("passportNumber").toString());
        if (payload.get("drivingLicenseNumber") != null) employee.setDrivingLicenseNumber(payload.get("drivingLicenseNumber").toString());

        if (payload.get("workLocation") != null) employee.setWorkLocation(payload.get("workLocation").toString());
        if (payload.get("profilePhoto") != null) employee.setProfilePhoto(payload.get("profilePhoto").toString());

        if (payload.get("dateOfBirth") != null) {
            try {
                employee.setDateOfBirth(java.time.LocalDate.parse(payload.get("dateOfBirth").toString()));
            } catch (Exception ignored) {}
        }

        Employee saved = employeeRepository.save(employee);
        log.info("Updated demographic profile for employee [{}]", saved.getEmployeeCode());
        return ApiResponse.success("Demographic profile updated successfully", saved);
    }
}

