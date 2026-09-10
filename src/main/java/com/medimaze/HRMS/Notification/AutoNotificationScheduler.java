package com.medimaze.HRMS.Notification;

import com.medimaze.HRMS.Employee.Employee;
import com.medimaze.HRMS.Employee.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoNotificationScheduler {

    private final EmployeeRepository employeeRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    // Runs every day at 00:05 AM and on application startup
    @Scheduled(cron = "0 5 0 * * ?")
    @EventListener(ApplicationReadyEvent.class)
    public void generateDailyAutomatedNotifications() {
        log.info("Running daily automated notification scheduler for Birthdays & Work Anniversaries...");
        generateBirthdayNotifications();
        generateWorkAnniversaryNotifications();
    }

    public void generateBirthdayNotifications() {
        try {
            LocalDate today = LocalDate.now();
            int currentMonth = today.getMonthValue();
            int currentDay = today.getDayOfMonth();

            List<Employee> allEmployees = employeeRepository.findAll();
            for (Employee emp : allEmployees) {
                if (Boolean.TRUE.equals(emp.getIsActive()) && emp.getDateOfBirth() != null) {
                    if (emp.getDateOfBirth().getMonthValue() == currentMonth && emp.getDateOfBirth().getDayOfMonth() == currentDay) {
                        String empName = emp.getFirstName() + " " + emp.getLastName();
                        String title = "🎂 Happy Birthday Wish!";
                        String message = "Wishing " + empName + " a very Happy Birthday today! Have a wonderful and blessed day!";

                        boolean exists = notificationRepository.findByScopeOrderByCreatedAtDesc("BROADCAST").stream()
                                .anyMatch(n -> "BIRTHDAY".equalsIgnoreCase(n.getCategory()) && n.getMessage() != null && n.getMessage().contains(empName));

                        if (!exists) {
                            notificationService.createBroadcastNotification(title, message, "BIRTHDAY", emp.getUnit(), emp.getOrganization(), emp);
                            log.info("Auto-generated Birthday broadcast notification for employee: {} in unit: {}", empName, emp.getUnit() != null ? emp.getUnit().getUnitName() : "All");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to generate birthday notifications: {}", e.getMessage());
        }
    }

    public void generateWorkAnniversaryNotifications() {
        try {
            LocalDate today = LocalDate.now();
            int currentMonth = today.getMonthValue();
            int currentDay = today.getDayOfMonth();

            List<Employee> allEmployees = employeeRepository.findAll();
            for (Employee emp : allEmployees) {
                if (Boolean.TRUE.equals(emp.getIsActive()) && emp.getJoiningDate() != null) {
                    if (emp.getJoiningDate().getMonthValue() == currentMonth 
                            && emp.getJoiningDate().getDayOfMonth() == currentDay
                            && emp.getJoiningDate().getYear() < today.getYear()) {
                        
                        int years = today.getYear() - emp.getJoiningDate().getYear();
                        String empName = emp.getFirstName() + " " + emp.getLastName();
                        String title = "🏆 Happy Work Anniversary!";
                        String message = "Congratulations to " + empName + " on completing " + years + " year" + (years > 1 ? "s" : "") + " with our organization! Thank you for your dedication!";

                        boolean exists = notificationRepository.findByScopeOrderByCreatedAtDesc("BROADCAST").stream()
                                .anyMatch(n -> "WORK_ANNIVERSARY".equalsIgnoreCase(n.getCategory()) && n.getMessage() != null && n.getMessage().contains(empName));

                        if (!exists) {
                            notificationService.createBroadcastNotification(title, message, "WORK_ANNIVERSARY", emp.getUnit(), emp.getOrganization(), emp);
                            log.info("Auto-generated Work Anniversary broadcast notification for employee: {} in unit: {}", empName, emp.getUnit() != null ? emp.getUnit().getUnitName() : "All");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to generate work anniversary notifications: {}", e.getMessage());
        }
    }
}
