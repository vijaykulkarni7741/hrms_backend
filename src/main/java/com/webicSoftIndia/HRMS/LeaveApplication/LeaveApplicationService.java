package com.webicSoftIndia.HRMS.LeaveApplication;

import com.webicSoftIndia.HRMS.Employee.Employee;
import com.webicSoftIndia.HRMS.Employee.EmployeeRepository;
import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.Notification.Notification;
import com.webicSoftIndia.HRMS.Notification.NotificationRepository;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.webicSoftIndia.HRMS.RuleConfig.HrmsRuleConfig;
import com.webicSoftIndia.HRMS.RuleConfig.HrmsRuleConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import com.webicSoftIndia.HRMS.LeaveType.LeaveType;
import com.webicSoftIndia.HRMS.LeaveType.LeaveTypeRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveApplicationService {

    private final LeaveApplicationRepository leaveApplicationRepository;
    private final EmployeeRepository employeeRepository;
    private final NotificationRepository notificationRepository;
    private final RedisSessionService redisSessionService;
    private final HrmsRuleConfigService ruleConfigService;
    private final LeaveTypeRepository leaveTypeRepository;

    @Transactional
    public ApiResponse<LeaveApplication> saveOrUpdateLeaveApplication(LeaveApplication application) {
        if (application == null) {
            return ApiResponse.error("LeaveApplication payload cannot be null");
        }

        // UPDATE MODE
        if (application.getId() != null && application.getId() > 0) {
            LeaveApplication existing = leaveApplicationRepository.findById(application.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("LeaveApplication not found with ID: " + application.getId());
            }

            if (application.getStartDate() != null) existing.setStartDate(application.getStartDate());
            if (application.getEndDate() != null) existing.setEndDate(application.getEndDate());
            if (application.getTotalDays() != null) existing.setTotalDays(application.getTotalDays());
            if (application.getReason() != null) existing.setReason(application.getReason());
            if (application.getStatus() != null) existing.setStatus(application.getStatus());
            if (application.getIsActive() != null) existing.setIsActive(application.getIsActive());

            LeaveApplication updated = leaveApplicationRepository.save(existing);
            return ApiResponse.success("LeaveApplication updated successfully", updated);
        }

        // CREATE MODE
        if (application.getEmployee() != null && application.getEmployee().getId() != null) {
            Employee emp = employeeRepository.findById(application.getEmployee().getId()).orElse(null);
            if (emp != null) {
                application.setEmployee(emp);
                if (application.getOrganization() == null) application.setOrganization(emp.getOrganization());
                if (application.getUnit() == null) application.setUnit(emp.getUnit());

                // Guarantee LeaveType is non-null
                LeaveType resolvedType = resolveLeaveType(application.getLeaveType(), application.getReason(), emp);
                application.setLeaveType(resolvedType);

                // 📌 HR Rule Enforcement: Planned Leave Advance Notice Rule
                String leaveTypeStr = "";
                if (application.getLeaveType() != null) {
                    if (application.getLeaveType().getTypeName() != null) {
                        leaveTypeStr = application.getLeaveType().getTypeName().toUpperCase();
                    } else if (application.getLeaveType().getTypeCode() != null) {
                        leaveTypeStr = application.getLeaveType().getTypeCode().toUpperCase();
                    }
                }

                Long unitId = emp.getUnit() != null ? emp.getUnit().getId() : null;
                HrmsRuleConfig ruleConfig = ruleConfigService.getEffectiveRuleConfig(unitId);

                // 🎯 Dynamic Monthly Leave Limit Rule Check
                Integer maxLeavesPerMonth = ruleConfig.getMaxLeaveApplicationsPerMonth();
                if (maxLeavesPerMonth != null && maxLeavesPerMonth > 0 && application.getStartDate() != null) {
                    LocalDate startOfMonth = application.getStartDate().withDayOfMonth(1);
                    LocalDate endOfMonth = application.getStartDate().withDayOfMonth(application.getStartDate().lengthOfMonth());
                    long existingLeavesCount = leaveApplicationRepository.countLeaveApplicationsInMonth(emp.getId(), startOfMonth, endOfMonth);
                    if (existingLeavesCount >= maxLeavesPerMonth) {
                        return ApiResponse.error("Leave request failed: Maximum limit of " + maxLeavesPerMonth 
                                + " leave application(s) for month " + application.getStartDate().getMonth() + " " + application.getStartDate().getYear() 
                                + " reached for your account as per dynamic HR policy rule.");
                    }
                }

                if (leaveTypeStr.contains("PLAN") || leaveTypeStr.contains("PAID") || leaveTypeStr.equals("PL")) {
                    Integer advanceDays = ruleConfig.getPlannedLeaveAdvanceDays();
                    if (advanceDays != null && advanceDays > 0) {
                        LocalDate startDate = application.getStartDate();
                        if (startDate != null) {
                            LocalDate minAllowedDate = LocalDate.now().plusDays(advanceDays);
                            if (startDate.isBefore(minAllowedDate)) {
                                return ApiResponse.error("Leave request generation failed: HR policy requires Planned Leave to be requested at least "
                                        + advanceDays + " days in advance. Earliest allowed leave start date is " + minAllowedDate + ".");
                            }
                        }
                    }
                }

                // Auto-map dual managers from Employee record
                Long repMgrId = emp.getReportingManagerId();
                Long wrkMgrId = emp.getWorkingManagerId() != null ? emp.getWorkingManagerId() : emp.getManagerId();

                application.setReportingManagerId(repMgrId);
                application.setWorkingManagerId(wrkMgrId);
                application.setReportingManagerStatus("PENDING");
                application.setWorkingManagerStatus("PENDING");
                application.setStatus("PENDING");
                application.setIsActive(true);

                LeaveApplication saved = leaveApplicationRepository.save(application);

                // 🔔 Notify Reporting Manager
                if (repMgrId != null && repMgrId > 0) {
                    sendNotification(repMgrId, emp,
                            "New Leave Request Notification",
                            "Employee " + emp.getFirstName() + " " + emp.getLastName() + " has requested leave from "
                                    + saved.getStartDate() + " to " + saved.getEndDate() + ". Please review and approve as Reporting Manager.",
                            "LEAVE_REQUEST");
                }

                // 🔔 Notify Working Manager
                if (wrkMgrId != null && wrkMgrId > 0 && !wrkMgrId.equals(repMgrId)) {
                    sendNotification(wrkMgrId, emp,
                            "New Leave Request Notification",
                            "Employee " + emp.getFirstName() + " " + emp.getLastName() + " has requested leave from "
                                    + saved.getStartDate() + " to " + saved.getEndDate() + ". Please review and approve as Working Manager.",
                            "LEAVE_REQUEST");
                }

                return ApiResponse.success("LeaveApplication created successfully and notified managers", saved);
            }
        }

        if (application.getStatus() == null) application.setStatus("PENDING");
        if (application.getIsActive() == null) application.setIsActive(true);

        LeaveApplication saved = leaveApplicationRepository.save(application);
        return ApiResponse.success("LeaveApplication created successfully", saved);
    }

    @Transactional
    public ApiResponse<LeaveApplication> processManagerApproval(Long applicationId, Long managerEmployeeId, String action) {
        if (applicationId == null || applicationId <= 0) {
            return ApiResponse.error("Valid LeaveApplication ID is required");
        }
        if (managerEmployeeId == null || managerEmployeeId <= 0) {
            return ApiResponse.error("Manager Employee ID is required");
        }
        if (action == null || action.trim().isEmpty()) {
            return ApiResponse.error("Action (APPROVED/REJECTED) is required");
        }

        LeaveApplication app = leaveApplicationRepository.findById(applicationId).orElse(null);
        if (app == null) {
            return ApiResponse.error("LeaveApplication not found with ID: " + applicationId);
        }

        String normalizedAction = action.trim().toUpperCase();
        boolean matched = false;

        if (managerEmployeeId.equals(app.getReportingManagerId())) {
            app.setReportingManagerStatus(normalizedAction);
            matched = true;
        }

        if (managerEmployeeId.equals(app.getWorkingManagerId())) {
            app.setWorkingManagerStatus(normalizedAction);
            matched = true;
        }

        if (!matched) {
            // Fallback: If manager is Admin/HR, allow approving both
            app.setReportingManagerStatus(normalizedAction);
            app.setWorkingManagerStatus(normalizedAction);
        }

        // Calculate overall status
        boolean repApp = "APPROVED".equalsIgnoreCase(app.getReportingManagerStatus());
        boolean repRej = "REJECTED".equalsIgnoreCase(app.getReportingManagerStatus());

        boolean wrkApp = "APPROVED".equalsIgnoreCase(app.getWorkingManagerStatus());
        boolean wrkRej = "REJECTED".equalsIgnoreCase(app.getWorkingManagerStatus());

        Employee emp = app.getEmployee();

        if (repRej || wrkRej) {
            app.setStatus("REJECTED");
            if (emp != null) {
                sendNotification(emp.getId(), emp,
                        "Leave Request Rejected",
                        "Your leave request from " + app.getStartDate() + " to " + app.getEndDate() + " has been REJECTED.",
                        "LEAVE_REJECTED");
            }
        } else if (repApp && wrkApp) {
            app.setStatus("APPROVED");
            if (emp != null) {
                sendNotification(emp.getId(), emp,
                        "Leave Request Approved by Both Managers!",
                        "Your leave request from " + app.getStartDate() + " to " + app.getEndDate()
                                + " has been APPROVED by both your Reporting Manager and Working Manager.",
                        "LEAVE_APPROVED");
            }
        } else if (repApp || wrkApp) {
            app.setStatus("PARTIALLY_APPROVED");
            if (emp != null) {
                sendNotification(emp.getId(), emp,
                        "Leave Request Partially Approved",
                        "Your leave request from " + app.getStartDate() + " to " + app.getEndDate()
                                + " was approved by one manager, awaiting second manager approval.",
                        "LEAVE_PARTIALLY_APPROVED");
            }
        }

        LeaveApplication updated = leaveApplicationRepository.save(app);
        return ApiResponse.success("Leave approval action processed successfully", updated);
    }

    @Transactional
    public ApiResponse<LeaveApplication> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid LeaveApplication ID is required");
        }
        LeaveApplication application = leaveApplicationRepository.findById(id).orElse(null);
        if (application == null) {
            return ApiResponse.error("LeaveApplication not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(application.getIsActive());
        application.setIsActive(newStatus);
        application.setStatus(newStatus ? "PENDING" : "CANCELLED");

        LeaveApplication updated = leaveApplicationRepository.save(application);
        return ApiResponse.success("LeaveApplication active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<LeaveApplication>> getLeaveApplicationsBySessionUnit(String sessionId) {
        List<LeaveApplication> list;
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
            if (sessionData != null) {
                Long empId = sessionData.getId();
                String roleCode = sessionData.getRoleCode();

                if (empId != null && empId > 0) {
                    list = leaveApplicationRepository.findByReportingManagerIdOrWorkingManagerId(empId, empId);
                    if (list != null && !list.isEmpty()) {
                        return ApiResponse.success("LeaveApplications retrieved successfully for manager", list);
                    }

                    // Fallback for Admin/HR roles if not assigned directly as reporting/working manager
                    if (roleCode != null && (roleCode.equalsIgnoreCase("ADMIN") || roleCode.equalsIgnoreCase("SUPER_ADMIN") || roleCode.equalsIgnoreCase("HR"))) {
                        Long unitId = sessionData.getUnitId();
                        if (unitId != null && unitId > 0) {
                            list = leaveApplicationRepository.findByUnitId(unitId);
                            return ApiResponse.success("LeaveApplications retrieved successfully for Admin/HR", list);
                        }
                    } else {
                        return ApiResponse.success("LeaveApplications retrieved successfully for manager", list != null ? list : java.util.Collections.emptyList());
                    }
                }

                Long unitId = sessionData.getUnitId();
                if (unitId != null && unitId > 0) {
                    list = leaveApplicationRepository.findByUnitId(unitId);
                    return ApiResponse.success("LeaveApplications retrieved successfully", list);
                }
            }
        }
        list = leaveApplicationRepository.findAll();
        return ApiResponse.success("LeaveApplications retrieved successfully", list);
    }

    private void sendNotification(Long recipientEmpId, Employee emp, String title, String message, String type) {
        try {
            Employee recipient = employeeRepository.findById(recipientEmpId).orElse(null);
            if (recipient == null) return;

            Notification notification = Notification.builder()
                    .organization(emp != null && emp.getOrganization() != null ? emp.getOrganization() : recipient.getOrganization())
                    .unit(emp != null && emp.getUnit() != null ? emp.getUnit() : recipient.getUnit())
                    .recipient(recipient)
                    .title(title)
                    .message(message)
                    .type(type)
                    .scope("PRIVATE")
                    .category("LEAVE")
                    .isRead(false)
                    .status("UNREAD")
                    .isActive(true)
                    .build();

            notificationRepository.save(notification);
            log.info("Sent Notification ID to Employee ID {}: {}", recipientEmpId, title);
        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }

    private LeaveType resolveLeaveType(LeaveType provided, String reason, Employee emp) {
        if (provided != null && provided.getId() != null && provided.getId() > 0) {
            LeaveType found = leaveTypeRepository.findById(provided.getId()).orElse(null);
            if (found != null) return found;
        }

        String r = reason != null ? reason.toUpperCase() : "";
        String code = (provided != null && provided.getTypeCode() != null) ? provided.getTypeCode().toUpperCase() : "";
        String name = (provided != null && provided.getTypeName() != null) ? provided.getTypeName().toUpperCase() : "";

        java.util.Optional<LeaveType> opt = java.util.Optional.empty();
        if (!code.isEmpty()) opt = leaveTypeRepository.findFirstByTypeCodeIgnoreCase(code);
        if (opt.isEmpty() && !name.isEmpty()) opt = leaveTypeRepository.findFirstByTypeNameIgnoreCase(name);

        if (opt.isEmpty()) {
            if (r.contains("CASUAL") || r.contains("CL")) {
                opt = leaveTypeRepository.findFirstByTypeNameIgnoreCase("Casual Leave")
                        .or(() -> leaveTypeRepository.findFirstByTypeCodeIgnoreCase("CL"));
            } else if (r.contains("SICK") || r.contains("SL")) {
                opt = leaveTypeRepository.findFirstByTypeNameIgnoreCase("Sick Leave")
                        .or(() -> leaveTypeRepository.findFirstByTypeCodeIgnoreCase("SL"));
            } else if (r.contains("PAID") || r.contains("PL")) {
                opt = leaveTypeRepository.findFirstByTypeNameIgnoreCase("Paid Leave")
                        .or(() -> leaveTypeRepository.findFirstByTypeCodeIgnoreCase("PL"));
            } else if (r.contains("COMP") || r.contains("WEEK-OFF")) {
                opt = leaveTypeRepository.findFirstByTypeNameIgnoreCase("Compensatory Off")
                        .or(() -> leaveTypeRepository.findFirstByTypeCodeIgnoreCase("COMP_OFF"));
            }
        }

        if (opt.isPresent()) return opt.get();

        if (emp != null && emp.getUnit() != null) {
            opt = leaveTypeRepository.findFirstByUnitIdAndIsActiveTrue(emp.getUnit().getId());
            if (opt.isPresent()) return opt.get();
        }

        List<LeaveType> all = leaveTypeRepository.findAll();
        if (!all.isEmpty()) return all.get(0);

        LeaveType defaultType = LeaveType.builder()
                .organization(emp != null ? emp.getOrganization() : null)
                .unit(emp != null ? emp.getUnit() : null)
                .typeName("Casual Leave")
                .typeCode("CL")
                .description("Casual Leave")
                .isPaid(true)
                .status("ACTIVE")
                .isActive(true)
                .build();
        return leaveTypeRepository.save(defaultType);
    }
}
