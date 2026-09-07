package com.webicSoftIndia.HRMS.common;

import com.webicSoftIndia.HRMS.Employee.Employee;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    public void sendResignationNotification(
            Employee employee,
            String resignationDate,
            String lastWorkingDay,
            String reason,
            String rmEmail,
            String wmEmail) {

        String empName = employee != null ? (employee.getFirstName() + " " + (employee.getLastName() != null ? employee.getLastName() : "")).trim() : "Employee";
        String empCode = employee != null && employee.getEmployeeCode() != null ? employee.getEmployeeCode() : "EMP";
        String designation = employee != null && employee.getDesignation() != null ? employee.getDesignation().getDesignationName() : "Team Member";
        String department = employee != null && employee.getDepartment() != null ? employee.getDepartment().getDepartmentName() : "Department";

        StringBuilder toEmailsBuilder = new StringBuilder();
        if (rmEmail != null && !rmEmail.trim().isEmpty()) {
            toEmailsBuilder.append(rmEmail.trim());
        }
        if (wmEmail != null && !wmEmail.trim().isEmpty()) {
            if (toEmailsBuilder.length() > 0) {
                toEmailsBuilder.append(", ");
            }
            toEmailsBuilder.append(wmEmail.trim());
        }
        String toEmails = toEmailsBuilder.toString();

        log.info("================================================================================");
        log.info("📧 DISPATCHING RESIGNATION NOTIFICATION EMAIL (DYNAMIC MANAGERS ONLY)");
        log.info("Timestamp : {}", LocalDateTime.now());
        log.info("TO        : {}", toEmails.isEmpty() ? "[No Manager Email Configured]" : toEmails);
        log.info("SUBJECT   : [Resignation Notice] Employee Resignation Submitted - {} ({})", empName, empCode);
        log.info("--------------------------------------------------------------------------------");
        log.info("Dear Manager(s),");
        log.info("Employee {} ({}) has submitted a Resignation Request.", empName, empCode);
        log.info("Designation       : {}", designation);
        log.info("Department        : {}", department);
        log.info("Resignation Date  : {}", resignationDate);
        log.info("Last Working Day  : {}", lastWorkingDay);
        log.info("Reason            : {}", reason);
        log.info("Status            : Pending Manager Review");
        log.info("================================================================================");
    }
}
