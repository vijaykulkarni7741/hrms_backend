package com.webicSoftIndia.HRMS.Attendance;

import com.webicSoftIndia.HRMS.Attendance.dto.MarkAttendanceRequest;
import com.webicSoftIndia.HRMS.Employee.Employee;
import com.webicSoftIndia.HRMS.Employee.EmployeeRepository;
import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.LeaveApplication.LeaveApplicationRepository;
import com.webicSoftIndia.HRMS.LeaveType.LeaveType;
import com.webicSoftIndia.HRMS.LeaveType.LeaveTypeRepository;
import com.webicSoftIndia.HRMS.Shift.Shift;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.webicSoftIndia.HRMS.RuleConfig.HrmsRuleConfig;
import com.webicSoftIndia.HRMS.RuleConfig.HrmsRuleConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.webicSoftIndia.HRMS.AttendanceRegularization.AttendanceRegularization;
import com.webicSoftIndia.HRMS.AttendanceRegularization.AttendanceRegularizationRepository;
import com.webicSoftIndia.HRMS.AttendanceApproval.AttendanceApproval;
import com.webicSoftIndia.HRMS.AttendanceApproval.AttendanceApprovalRepository;
import com.webicSoftIndia.HRMS.LeaveApplication.LeaveApplication;
import com.webicSoftIndia.HRMS.Notification.Notification;
import com.webicSoftIndia.HRMS.Notification.NotificationRepository;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final RedisSessionService redisSessionService;
    private final HrmsRuleConfigService ruleConfigService;
    private final AttendanceRegularizationRepository regularizationRepository;
    private final AttendanceApprovalRepository approvalRepository;
    private final LeaveApplicationRepository leaveApplicationRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final com.webicSoftIndia.HRMS.Roster.EmployeeRosterService rosterService;
    private final NotificationRepository notificationRepository;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            EmployeeRepository employeeRepository,
            RedisSessionService redisSessionService,
            HrmsRuleConfigService ruleConfigService,
            AttendanceRegularizationRepository regularizationRepository,
            AttendanceApprovalRepository approvalRepository,
            LeaveApplicationRepository leaveApplicationRepository,
            LeaveTypeRepository leaveTypeRepository,
            com.webicSoftIndia.HRMS.Roster.EmployeeRosterService rosterService,
            NotificationRepository notificationRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.redisSessionService = redisSessionService;
        this.ruleConfigService = ruleConfigService;
        this.regularizationRepository = regularizationRepository;
        this.approvalRepository = approvalRepository;
        this.leaveApplicationRepository = leaveApplicationRepository;
        this.leaveTypeRepository = leaveTypeRepository;
        this.rosterService = rosterService;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public ApiResponse<Attendance> markCheckIn(MarkAttendanceRequest request, String sessionId) {
        Long empId = resolveEmployeeId(request != null ? request.getEmployeeId() : null, sessionId);
        if (empId == null || empId <= 0) {
            return ApiResponse.error("Employee ID could not be determined from session or request payload");
        }

        Employee employee = employeeRepository.findById(empId).orElse(null);
        if (employee == null) {
            return ApiResponse.error("Employee record not found with ID: " + empId);
        }

        LocalDate today = LocalDate.now();
        Optional<Attendance> existingOpt = attendanceRepository.findTopByEmployeeIdAndAttendanceDateOrderByCreatedDateDesc(empId, today);
        if (existingOpt.isPresent() && existingOpt.get().getInTime() != null) {
            return ApiResponse.success("Already checked in for today", existingOpt.get());
        }

        // Night shift duplicate prevention:
        // If employee has an OPEN check-in from yesterday (no checkout yet), block a new check-in.
        // Example: shift 10 PM - 2 AM. At 1 AM (next day) employee accidentally taps check-in again.
        LocalDate yesterday = today.minusDays(1);
        Optional<Attendance> yesterdayOpenOpt = attendanceRepository
                .findTopByEmployeeIdAndAttendanceDateOrderByCreatedDateDesc(empId, yesterday);
        if (yesterdayOpenOpt.isPresent()
                && yesterdayOpenOpt.get().getInTime() != null
                && yesterdayOpenOpt.get().getOutTime() == null) {
            log.info("Night shift still open for employee ID {} - blocking duplicate check-in", empId);
            return ApiResponse.success(
                    "Your night shift is still in progress (started " + yesterday + "). Please Check-Out first.",
                    yesterdayOpenOpt.get());
        }

        // Resolve the shift for today (check-in date). Roster service returns the assigned shift
        // from the roster or falls back to the employee's default shift.
        com.webicSoftIndia.HRMS.Roster.EmployeeRosterService.RosterScheduleResolved resolved =
                rosterService.resolveEffectiveSchedule(employee, today, null);

        LocalTime now = LocalTime.now();
        LocalTime standardInTime = (resolved != null && resolved.getShift() != null && resolved.getShift().getStartTime() != null)
                ? parseTime(String.valueOf(resolved.getShift().getStartTime()))
                : LocalTime.of(9, 30);
        if (standardInTime == null) standardInTime = LocalTime.of(9, 30);

        // Cross-midnight late check:
        // For night shifts (startTime > 12:00), compare correctly across midnight.
        // e.g. shift starts 22:00, grace = 15 min → late if after 22:15 on the same side.
        // If standardInTime is PM and now is AM (next calendar day), employee is very late.
        boolean isLate;
        if (standardInTime.isAfter(LocalTime.NOON) && now.isBefore(LocalTime.NOON)) {
            // now is on the AM side of midnight, shift start was PM → employee is late
            isLate = true;
        } else {
            isLate = now.isAfter(standardInTime.plusMinutes(15));
        }

        Attendance record = existingOpt.orElseGet(() -> Attendance.builder()
                .employee(employee)
                .unit(employee.getUnit())
                .attendanceDate(today)
                .isActive(true)
                .build());

        if (resolved != null && resolved.getShift() != null) {
            record.setShift(resolved.getShift());
        }

        record.setInTime(now);
        record.setStatus(isLate ? "LATE" : "PRESENT");
        record.setIsLate(isLate);

        if (request != null) {
            if (request.getSelfieImage() != null && !request.getSelfieImage().trim().isEmpty()) {
                record.setCheckInSelfie(request.getSelfieImage().trim());
            }
            if (request.getLocation() != null && !request.getLocation().trim().isEmpty()) {
                record.setCheckInLocation(request.getLocation().trim());
            } else if (employee.getWorkLocation() != null) {
                record.setCheckInLocation(employee.getWorkLocation());
            }
            if (request.getRemarks() != null) {
                record.setRemarks(request.getRemarks());
            }
        }

        Attendance saved = attendanceRepository.save(record);
        log.info("Employee ID {} successfully checked in at {} with selfie photo", empId, now);

        sendAttendanceManagerNotification(employee, saved, resolved, "CHECK_IN");

        return ApiResponse.success("Check-In marked successfully with live selfie", saved);
    }

    @Transactional
    public ApiResponse<Attendance> markCheckOut(MarkAttendanceRequest request, String sessionId) {
        Long empId = resolveEmployeeId(request != null ? request.getEmployeeId() : null, sessionId);
        if (empId == null || empId <= 0) {
            return ApiResponse.error("Employee ID could not be determined from session or request payload");
        }

        LocalDate today = LocalDate.now();
        Optional<Attendance> existingOpt = attendanceRepository.findTopByEmployeeIdAndAttendanceDateOrderByCreatedDateDesc(empId, today);

        // Night-shift / cross-midnight support:
        // If no open check-in found for today, look back to yesterday.
        // Scenario: Employee shift starts 10 PM (Sep 5) and ends 2 AM (Sep 6).
        // At 2 AM Sep 6, LocalDate.now() = Sep 6, but the check-in record is dated Sep 5.
        boolean todayHasOpenCheckIn = existingOpt.isPresent()
                && existingOpt.get().getInTime() != null
                && existingOpt.get().getOutTime() == null;

        if (!todayHasOpenCheckIn) {
            LocalDate yesterday = today.minusDays(1);
            Optional<Attendance> yesterdayOpt = attendanceRepository
                    .findTopByEmployeeIdAndAttendanceDateOrderByCreatedDateDesc(empId, yesterday);
            // Use yesterday's record only if it has an open check-in (no checkout yet)
            if (yesterdayOpt.isPresent()
                    && yesterdayOpt.get().getInTime() != null
                    && yesterdayOpt.get().getOutTime() == null) {
                existingOpt = yesterdayOpt;
                log.info("Night shift detected: using previous day's attendance record for employee ID {}", empId);
            }
        }

        if (existingOpt.isEmpty() || existingOpt.get().getInTime() == null) {
            return ApiResponse.error("You must Check-In first before marking Check-Out for today");
        }

        Attendance record = existingOpt.get();
        Employee employee = record.getEmployee();
        LocalTime now = LocalTime.now();
        record.setOutTime(now);

        // CRITICAL: Use the attendance record's own date (check-in date) for roster resolution.
        // For night shifts: record.getAttendanceDate() = Sep 5 (check-in day).
        // today = Sep 6 (checkout day). Resolving roster for Sep 6 would return the wrong shift.
        LocalDate attendanceDate = record.getAttendanceDate() != null ? record.getAttendanceDate() : today;
        com.webicSoftIndia.HRMS.Roster.EmployeeRosterService.RosterScheduleResolved resolved =
                rosterService.resolveEffectiveSchedule(employee, attendanceDate, null);

        // CRITICAL: Prefer the shift stored in the attendance record at check-in time.
        // Do NOT overwrite it — the check-in already stored the correct allocated shift.
        // Only assign a shift if none was stored during check-in.
        com.webicSoftIndia.HRMS.Shift.Shift activeShift = record.getShift();
        if (activeShift == null && resolved != null && resolved.getShift() != null) {
            activeShift = resolved.getShift();
            record.setShift(activeShift);
            log.info("Checkout: no shift in record for employee ID {}, using roster-resolved shift: {}",
                    empId, activeShift.getShiftName());
        } else if (activeShift != null) {
            log.info("Checkout: using check-in stored shift '{}' for employee ID {} (attendance date: {})",
                    activeShift.getShiftName(), empId, attendanceDate);
        }

        // Determine the expected end time from the active shift (the one set at check-in)
        LocalTime standardOutTime = (activeShift != null && activeShift.getEndTime() != null)
                ? activeShift.getEndTime()
                : LocalTime.of(18, 30);

        // Cross-midnight early departure check:
        // For night shifts ending at e.g. 02:00, both standardOutTime and now are AM times,
        // so the standard isBefore() comparison works correctly.
        // Edge: if shift ends at 02:00 and employee checks out at 01:45 → early (true) ✅
        //       if shift ends at 02:00 and employee checks out at 02:10 → not early (false) ✅
        boolean isEarlyDeparture = now.isBefore(standardOutTime);
        record.setIsEarlyDeparture(isEarlyDeparture);

        double requiredHours = calculateRequiredShiftHours(resolved);
        if (record.getInTime() != null) {
            long minutes = Duration.between(record.getInTime(), now).toMinutes();
            // Cross-midnight fix: if checkout time is before check-in time (e.g., in=22:00, out=02:00),
            // Duration gives a negative value. Add 24 hours (1440 minutes) to get correct 4h duration.
            if (minutes < 0) {
                minutes += 24 * 60;
            }
            double hours = minutes / 60.0;
            record.setTotalHours(BigDecimal.valueOf(hours).setScale(2, RoundingMode.HALF_UP));
        }

        if (request != null) {
            if (request.getSelfieImage() != null && !request.getSelfieImage().trim().isEmpty()) {
                record.setCheckOutSelfie(request.getSelfieImage().trim());
            }
            if (request.getLocation() != null && !request.getLocation().trim().isEmpty()) {
                record.setCheckOutLocation(request.getLocation().trim());
            }
            if (request.getRemarks() != null) {
                record.setRemarks(request.getRemarks());
            }
        }

        Attendance saved = attendanceRepository.save(record);
        log.info("Employee ID {} successfully checked out at {} with selfie photo", empId, now);

        // For notification: if we used a stored shift (not roster-resolved), rebuild resolved with correct shift info
        if (activeShift != null && (resolved == null || resolved.getShift() == null || !activeShift.getId().equals(
                resolved.getShift() != null ? resolved.getShift().getId() : null))) {
            resolved = com.webicSoftIndia.HRMS.Roster.EmployeeRosterService.RosterScheduleResolved.builder()
                    .shiftId(activeShift.getId())
                    .shift(activeShift)
                    .shiftName(activeShift.getShiftName())
                    .shiftTiming(activeShift.getStartTime() + " - " + activeShift.getEndTime())
                    .weekOffDay1(resolved != null ? resolved.getWeekOffDay1() : "SATURDAY")
                    .weekOffDay2(resolved != null ? resolved.getWeekOffDay2() : "SUNDAY")
                    .source("ATTENDANCE_RECORD_SHIFT")
                    .build();
        }

        sendAttendanceManagerNotification(employee, saved, resolved, "CHECK_OUT");

        return ApiResponse.success("Check-Out marked successfully with live selfie", saved);
    }

    @Transactional(readOnly = true)
    public ApiResponse<Attendance> getTodayAttendance(String sessionId) {
        Long empId = resolveEmployeeId(null, sessionId);
        if (empId == null || empId <= 0) {
            return ApiResponse.error("Employee session invalid");
        }

        LocalDate today = LocalDate.now();
        Optional<Attendance> todayOpt = attendanceRepository.findTopByEmployeeIdAndAttendanceDateOrderByCreatedDateDesc(empId, today);

        // Night-shift support: if no record for today, check yesterday for an ongoing (open) shift
        if (todayOpt.isEmpty() || todayOpt.get().getInTime() == null) {
            LocalDate yesterday = today.minusDays(1);
            Optional<Attendance> yesterdayOpt = attendanceRepository
                    .findTopByEmployeeIdAndAttendanceDateOrderByCreatedDateDesc(empId, yesterday);
            if (yesterdayOpt.isPresent()
                    && yesterdayOpt.get().getInTime() != null
                    && yesterdayOpt.get().getOutTime() == null) {
                // Return the ongoing night-shift record
                return ApiResponse.success("Night shift in progress (started previous day)", yesterdayOpt.get());
            }
        }

        return todayOpt.map(attendance -> ApiResponse.success("Today's attendance record retrieved", attendance))
                .orElseGet(() -> ApiResponse.success("No attendance record marked for today yet", null));
    }

    @Transactional
    public ApiResponse<Attendance> saveOrUpdateAttendance(Attendance attendance) {
        if (attendance == null) {
            return ApiResponse.error("Attendance payload cannot be null");
        }

        if (attendance.getId() != null && attendance.getId() > 0) {
            Attendance existing = attendanceRepository.findById(attendance.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Attendance record not found with ID: " + attendance.getId());
            }

            if (attendance.getInTime() != null) existing.setInTime(attendance.getInTime());
            if (attendance.getOutTime() != null) existing.setOutTime(attendance.getOutTime());
            if (attendance.getCheckInSelfie() != null) existing.setCheckInSelfie(attendance.getCheckInSelfie());
            if (attendance.getCheckOutSelfie() != null) existing.setCheckOutSelfie(attendance.getCheckOutSelfie());
            if (attendance.getCheckInLocation() != null) existing.setCheckInLocation(attendance.getCheckInLocation());
            if (attendance.getCheckOutLocation() != null) existing.setCheckOutLocation(attendance.getCheckOutLocation());
            if (attendance.getTotalHours() != null) existing.setTotalHours(attendance.getTotalHours());
            if (attendance.getStatus() != null) existing.setStatus(attendance.getStatus());
            if (attendance.getIsLate() != null) existing.setIsLate(attendance.getIsLate());
            if (attendance.getIsEarlyDeparture() != null) existing.setIsEarlyDeparture(attendance.getIsEarlyDeparture());
            if (attendance.getOvertimeHours() != null) existing.setOvertimeHours(attendance.getOvertimeHours());
            if (attendance.getRemarks() != null) existing.setRemarks(attendance.getRemarks());
            if (attendance.getIsActive() != null) existing.setIsActive(attendance.getIsActive());

            Attendance updated = attendanceRepository.save(existing);
            return ApiResponse.success("Attendance updated successfully", updated);
        }

        if (attendance.getAttendanceDate() == null) {
            attendance.setAttendanceDate(LocalDate.now());
        }
        if (attendance.getStatus() == null) {
            attendance.setStatus("PRESENT");
        }
        if (attendance.getIsActive() == null) {
            attendance.setIsActive(true);
        }

        Attendance saved = attendanceRepository.save(attendance);
        return ApiResponse.success("Attendance created successfully", saved);
    }

    @Transactional
    public ApiResponse<Attendance> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Attendance ID is required");
        }
        Attendance attendance = attendanceRepository.findById(id).orElse(null);
        if (attendance == null) {
            return ApiResponse.error("Attendance record not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(attendance.getIsActive());
        attendance.setIsActive(newStatus);
        attendance.setStatus(newStatus ? "PRESENT" : "INACTIVE");

        Attendance updated = attendanceRepository.save(attendance);
        return ApiResponse.success("Attendance active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Attendance>> getAttendanceBySessionUnit(String sessionId) {
        Long empId = resolveEmployeeId(null, sessionId);
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
            if (sessionData != null) {
                Long unitId = sessionData.getUnitId();
                if (unitId != null && unitId > 0) {
                    List<Attendance> list = attendanceRepository.findByUnitId(unitId);
                    return ApiResponse.success("Attendance records retrieved successfully", list);
                }
            }
        }
        if (empId != null && empId > 0) {
            List<Attendance> list = attendanceRepository.findByEmployeeIdOrderByAttendanceDateDesc(empId);
            return ApiResponse.success("Employee attendance records retrieved successfully", list);
        }
        List<Attendance> list = attendanceRepository.findAll();
        return ApiResponse.success("Attendance records retrieved successfully", list);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Attendance>> getEmployeeMonthlyAttendanceLogs(String sessionId, Integer month, Integer year) {
        return getEmployeeMonthlyAttendanceLogs(sessionId, month, year, null, null, null);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Attendance>> getEmployeeMonthlyAttendanceLogs(String sessionId, Integer month, Integer year, Integer days) {
        return getEmployeeMonthlyAttendanceLogs(sessionId, month, year, days, null, null);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Attendance>> getEmployeeMonthlyAttendanceLogs(String sessionId, Integer month, Integer year, Integer days, String fromDateStr, String toDateStr) {
        Long empId = resolveEmployeeId(null, sessionId);
        if (empId == null || empId <= 0) {
            return ApiResponse.error("Employee session invalid");
        }

        LocalDate now = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;

        if (fromDateStr != null && !fromDateStr.trim().isEmpty() && toDateStr != null && !toDateStr.trim().isEmpty()) {
            try {
                startDate = LocalDate.parse(fromDateStr.trim());
                endDate = LocalDate.parse(toDateStr.trim());
            } catch (Exception e) {
                return ApiResponse.error("Invalid fromDate/toDate format. Use YYYY-MM-DD");
            }
        } else if (days != null && days > 0) {
            endDate = now;
            startDate = now.minusDays(days - 1);
        } else {
            int targetMonth = (month != null && month >= 1 && month <= 12) ? month : now.getMonthValue();
            int targetYear = (year != null && year >= 2000) ? year : now.getYear();

            YearMonth ym = YearMonth.of(targetYear, targetMonth);
            startDate = ym.atDay(1);
            endDate = ym.atEndOfMonth();
        }

        List<Attendance> existingLogs = attendanceRepository.findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(empId, startDate, endDate);
        Map<LocalDate, Attendance> logMap = new HashMap<>();
        for (Attendance a : existingLogs) {
            if (a.getAttendanceDate() != null) {
                logMap.put(a.getAttendanceDate(), a);
            }
        }

        Employee employee = employeeRepository.findById(empId).orElse(null);

        // Fetch Leave Applications for leave status evaluation
        Map<LocalDate, String> leaveStatusMap = new HashMap<>();
        if (empId != null && empId > 0) {
            List<LeaveApplication> leaveApps = leaveApplicationRepository.findByEmployeeId(empId);
            for (LeaveApplication la : leaveApps) {
                if (la.getStartDate() != null && la.getEndDate() != null) {
                    String leaveStatusName = "APPROVED".equalsIgnoreCase(la.getStatus()) ? "LEAVE" : "LEAVE REQUESTED";
                    for (LocalDate d = la.getStartDate(); !d.isAfter(la.getEndDate()); d = d.plusDays(1)) {
                        if ("LEAVE".equals(leaveStatusName) || !leaveStatusMap.containsKey(d)) {
                            leaveStatusMap.put(d, leaveStatusName);
                        }
                    }
                }
            }
        }

        List<Attendance> fullList = new ArrayList<>();
        LocalDate limitDate = endDate.isAfter(now) ? now : endDate;

        for (LocalDate d = limitDate; !d.isBefore(startDate); d = d.minusDays(1)) {
            com.webicSoftIndia.HRMS.Roster.EmployeeRosterService.RosterScheduleResolved resolved = rosterService.resolveEffectiveSchedule(employee, d, null);
            String w1 = resolved.getWeekOffDay1() != null ? resolved.getWeekOffDay1().trim().toUpperCase() : "SATURDAY";
            String w2 = resolved.getWeekOffDay2() != null ? resolved.getWeekOffDay2().trim().toUpperCase() : "SUNDAY";
            boolean isWeekOff = d.getDayOfWeek().name().equalsIgnoreCase(w1) || d.getDayOfWeek().name().equalsIgnoreCase(w2);

            if (logMap.containsKey(d)) {
                Attendance record = logMap.get(d);
                if (leaveStatusMap.containsKey(d)) {
                    record.setStatus(leaveStatusMap.get(d));
                } else if (record.getInTime() != null && record.getOutTime() == null) {
                    record.setStatus("MISSED_CHECK_OUT");
                } else if (record.getInTime() == null && record.getOutTime() != null) {
                    record.setStatus("MISSED_CHECK_IN");
                } else if (isWeekOff && record.getInTime() != null && record.getOutTime() != null) {
                    String existingStatus = record.getStatus() != null ? record.getStatus().toUpperCase() : "";
                    if (!"COMP_OFF_USED".equals(existingStatus) && !"COMP_OFF_REQUESTED".equals(existingStatus)) {
                        record.setStatus("WEEK_OFF_WORKED");
                        if (record.getRemarks() == null || record.getRemarks().trim().isEmpty()) {
                            record.setRemarks("Worked on Week-Off: Eligible for Comp-Off");
                        }
                    }
                }
                fullList.add(record);
            } else {
                String computedStatus;
                String remark;

                if (leaveStatusMap.containsKey(d)) {
                    computedStatus = leaveStatusMap.get(d);
                    remark = "Leave record: " + computedStatus;
                } else if (isWeekOff) {
                    computedStatus = "WEEK_OFF";
                    remark = "Scheduled Week-Off";
                } else {
                    computedStatus = "ABSENT";
                    remark = "No check-in record found for workday";
                }

                Attendance absentRecord = Attendance.builder()
                        .employee(employee)
                        .unit(employee != null ? employee.getUnit() : null)
                        .attendanceDate(d)
                        .status(computedStatus)
                        .isActive(true)
                        .remarks(remark)
                        .build();
                fullList.add(absentRecord);
            }
        }

        return ApiResponse.success("Attendance logs retrieved successfully", fullList);
    }

    @Transactional
    public ApiResponse<Attendance> applyCompOff(String attendanceDateStr, String targetLeaveDateStr, String reason, String sessionId) {
        Long empId = resolveEmployeeId(null, sessionId);
        if (empId == null || empId <= 0) {
            return ApiResponse.error("Employee session invalid");
        }

        if (attendanceDateStr == null || attendanceDateStr.trim().isEmpty()) {
            return ApiResponse.error("Attendance date (Week-Off worked) is required for Comp-Off application");
        }

        LocalDate targetDate;
        try {
            targetDate = LocalDate.parse(attendanceDateStr.trim());
        } catch (Exception e) {
            return ApiResponse.error("Invalid attendance date format: " + attendanceDateStr);
        }

        LocalDate leaveTargetDate = targetDate;
        if (targetLeaveDateStr != null && !targetLeaveDateStr.trim().isEmpty()) {
            try {
                leaveTargetDate = LocalDate.parse(targetLeaveDateStr.trim());
            } catch (Exception e) {
                log.warn("Invalid target leave date format: {}, fallback to week-off date", targetLeaveDateStr);
            }
        }

        Employee employee = employeeRepository.findById(empId).orElse(null);
        if (employee == null) {
            return ApiResponse.error("Employee record not found");
        }

        Attendance attRecord = attendanceRepository.findTopByEmployeeIdAndAttendanceDateOrderByCreatedDateDesc(empId, targetDate)
                .orElseGet(() -> Attendance.builder()
                        .employee(employee)
                        .unit(employee.getUnit())
                        .attendanceDate(targetDate)
                        .isActive(true)
                        .build());

        attRecord.setStatus("COMP_OFF_USED");
        String note = "Comp-Off Applied for Week-Off Worked on " + targetDate + " (Leave requested for " + leaveTargetDate + ")";
        if (reason != null && !reason.trim().isEmpty()) {
            note += ": " + reason.trim();
        }
        attRecord.setRemarks(note);
        Attendance saved = attendanceRepository.save(attRecord);

        LeaveType compOffLeaveType = resolveOrCreateCompOffLeaveType(employee);

        LeaveApplication la = LeaveApplication.builder()
                .employee(employee)
                .organization(employee.getOrganization())
                .unit(employee.getUnit())
                .leaveType(compOffLeaveType)
                .startDate(leaveTargetDate)
                .endDate(leaveTargetDate)
                .totalDays(BigDecimal.ONE)
                .reason(note)
                .reportingManagerId(employee.getReportingManagerId())
                .workingManagerId(employee.getWorkingManagerId() != null ? employee.getWorkingManagerId() : employee.getManagerId())
                .reportingManagerStatus("PENDING")
                .workingManagerStatus("PENDING")
                .status("PENDING")
                .isActive(true)
                .build();
        leaveApplicationRepository.save(la);

        log.info("Employee ID {} applied for Comp-Off leave on {} for Week-Off worked on {}", empId, leaveTargetDate, targetDate);
        return ApiResponse.success("Comp-Off leave application submitted successfully for " + leaveTargetDate + " pending manager approval", saved);
    }

    @Transactional
    public ApiResponse<Attendance> regularizeAttendance(Long attendanceId, String attendanceDateStr, String inTimeStr, String outTimeStr, String reason, String sessionId) {
        Long empId = resolveEmployeeId(null, sessionId);
        if (empId == null || empId <= 0) {
            return ApiResponse.error("Employee session invalid");
        }

        Employee employee = employeeRepository.findById(empId).orElse(null);
        if (employee == null) {
            return ApiResponse.error("Employee record not found");
        }

        Long unitId = employee.getUnit() != null ? employee.getUnit().getId() : null;
        HrmsRuleConfig ruleConfig = ruleConfigService.getEffectiveRuleConfig(unitId);

        Integer maxDays = ruleConfig.getRegularizationMaxDays();
        if (maxDays == null || maxDays < 1) maxDays = 7;

        Integer maxRegsPerMonth = ruleConfig.getMaxRegularizationsPerMonth();
        if (maxRegsPerMonth == null || maxRegsPerMonth < 0) maxRegsPerMonth = 3;

        Boolean allowSameDay = ruleConfig.getAllowSameDayRegularization();
        if (allowSameDay == null) allowSameDay = true;

        Boolean allowFuture = ruleConfig.getAllowFutureDateRegularization();
        if (allowFuture == null) allowFuture = false;

        LocalDate targetDate = null;
        if (attendanceDateStr != null && !attendanceDateStr.trim().isEmpty()) {
            try {
                targetDate = LocalDate.parse(attendanceDateStr.trim());
            } catch (Exception e) {
                log.warn("Invalid attendance date format: {}", attendanceDateStr);
            }
        }

        Attendance existingRecord = null;
        if (attendanceId != null && attendanceId > 0) {
            existingRecord = attendanceRepository.findById(attendanceId).orElse(null);
            if (existingRecord != null) {
                targetDate = existingRecord.getAttendanceDate();
            }
        }

        if (targetDate == null) {
            return ApiResponse.error("Attendance date is required for regularization");
        }

        LocalDate today = LocalDate.now();

        if (targetDate.isAfter(today) && !allowFuture) {
            return ApiResponse.error("Regularization request failed: Future date regularization is not allowed as per dynamic HR policy rule.");
        }

        if (targetDate.equals(today) && !allowSameDay) {
            return ApiResponse.error("Regularization request failed: Same-day regularization is currently disabled as per dynamic HR policy rule.");
        }

        long daysDiff = Duration.between(targetDate.atStartOfDay(), today.atStartOfDay()).toDays();
        if (daysDiff > maxDays) {
            return ApiResponse.error("Regularization request failed: Missed punch regularization can only be submitted within " + maxDays + " days of the attendance date as per HR policy.");
        }

        // 🎯 Dynamic Monthly Limit Rule Check (e.g., maximum 3 requests per month)
        LocalDate startOfMonth = targetDate.withDayOfMonth(1);
        LocalDate endOfMonth = targetDate.withDayOfMonth(targetDate.lengthOfMonth());
        Long excludeId = existingRecord != null ? existingRecord.getId() : null;
        long monthlyRegCount = attendanceRepository.countRegularizationsInMonth(empId, startOfMonth, endOfMonth, excludeId);
        if (monthlyRegCount >= maxRegsPerMonth) {
            return ApiResponse.error("Regularization request failed: Maximum limit of " + maxRegsPerMonth 
                    + " regularization request(s) for month " + targetDate.getMonth() + " " + targetDate.getYear() 
                    + " reached for your account as per dynamic HR policy rule.");
        }

        final LocalDate finalTargetDate = targetDate;
        if (existingRecord == null) {
            Optional<Attendance> opt = attendanceRepository.findTopByEmployeeIdAndAttendanceDateOrderByCreatedDateDesc(empId, finalTargetDate);
            existingRecord = opt.orElseGet(() -> Attendance.builder()
                    .employee(employee)
                    .unit(employee.getUnit())
                    .attendanceDate(finalTargetDate)
                    .isActive(true)
                    .build());
        }

        LocalTime inTime = (inTimeStr != null && !inTimeStr.trim().isEmpty()) ? LocalTime.parse(inTimeStr.trim()) : LocalTime.of(9, 30);
        LocalTime outTime = (outTimeStr != null && !outTimeStr.trim().isEmpty()) ? LocalTime.parse(outTimeStr.trim()) : LocalTime.of(18, 30);

        if (existingRecord.getStatus() == null || existingRecord.getStatus().trim().isEmpty() || "ABSENT".equalsIgnoreCase(existingRecord.getStatus())) {
            existingRecord.setStatus("PENDING_REGULARIZATION");
        }
        Attendance savedAttendance = attendanceRepository.save(existingRecord);

        AttendanceRegularization regularization = AttendanceRegularization.builder()
                .attendance(savedAttendance)
                .employee(employee)
                .requestDate(today)
                .regularizationDate(finalTargetDate)
                .requestedInTime(inTime)
                .requestedOutTime(outTime)
                .reason(reason)
                .status("PENDING")
                .build();
        AttendanceRegularization savedReg = regularizationRepository.save(regularization);

        Long managerId = employee.getReportingManagerId();
        if (managerId == null) managerId = employee.getManagerId();
        if (managerId == null) managerId = employee.getWorkingManagerId();

        Employee approver = null;
        if (managerId != null && managerId > 0) {
            approver = employeeRepository.findById(managerId).orElse(null);
        }

        if (approver != null) {
            AttendanceApproval approval = AttendanceApproval.builder()
                    .referenceType("REGULARIZATION")
                    .referenceId(savedReg.getId())
                    .approver(approver)
                    .approvalLevel(1)
                    .status("PENDING")
                    .build();
            approvalRepository.save(approval);
        }

        log.info("Employee ID {} submitted attendance regularization request for date {} (Pending Manager Approval)", empId, targetDate);
        return ApiResponse.success("Attendance regularization request submitted successfully and pending manager approval", savedAttendance);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Attendance>> getUnconsumedWeekOffs(String sessionId) {
        Long empId = resolveEmployeeId(null, sessionId);
        if (empId == null || empId <= 0) {
            return ApiResponse.error("Employee session invalid");
        }

        Employee employee = employeeRepository.findById(empId).orElse(null);
        if (employee == null) {
            return ApiResponse.error("Employee record not found");
        }

        List<Attendance> allLogs = attendanceRepository.findByEmployeeIdOrderByAttendanceDateDesc(empId);
        List<Attendance> unconsumed = new ArrayList<>();

        for (Attendance a : allLogs) {
            if (a.getAttendanceDate() == null) continue;
            String status = a.getStatus() != null ? a.getStatus().toUpperCase() : "";

            if (status.equals("COMP_OFF_USED") || status.equals("COMP_OFF_REQUESTED")) {
                continue;
            }

            com.webicSoftIndia.HRMS.Roster.EmployeeRosterService.RosterScheduleResolved resolved = rosterService.resolveEffectiveSchedule(employee, a.getAttendanceDate(), null);
            String w1 = resolved.getWeekOffDay1() != null ? resolved.getWeekOffDay1().trim().toUpperCase() : "SATURDAY";
            String w2 = resolved.getWeekOffDay2() != null ? resolved.getWeekOffDay2().trim().toUpperCase() : "SUNDAY";

            boolean isWeekOff = a.getAttendanceDate().getDayOfWeek().name().equalsIgnoreCase(w1) 
                             || a.getAttendanceDate().getDayOfWeek().name().equalsIgnoreCase(w2);

            if (status.equals("WEEK_OFF_WORKED") || status.equals("WEEK_OFF_PRESENT") || 
               (isWeekOff && a.getInTime() != null && a.getOutTime() != null)) {
                unconsumed.add(a);
            }
        }

        return ApiResponse.success("Unconsumed week-off worked dates retrieved successfully", unconsumed);
    }

    private LeaveType resolveOrCreateCompOffLeaveType(Employee employee) {
        Optional<LeaveType> opt = leaveTypeRepository.findFirstByTypeCodeIgnoreCase("COMP_OFF");
        if (opt.isPresent()) return opt.get();

        opt = leaveTypeRepository.findFirstByTypeNameIgnoreCase("Compensatory Off");
        if (opt.isPresent()) return opt.get();

        if (employee != null && employee.getUnit() != null) {
            opt = leaveTypeRepository.findFirstByUnitIdAndIsActiveTrue(employee.getUnit().getId());
            if (opt.isPresent()) return opt.get();
        }

        List<LeaveType> all = leaveTypeRepository.findAll();
        if (!all.isEmpty()) return all.get(0);

        LeaveType compOff = LeaveType.builder()
                .organization(employee != null ? employee.getOrganization() : null)
                .unit(employee != null ? employee.getUnit() : null)
                .typeName("Compensatory Off")
                .typeCode("COMP_OFF")
                .description("Compensatory Off Leave")
                .isPaid(true)
                .status("ACTIVE")
                .isActive(true)
                .build();
        return leaveTypeRepository.save(compOff);
    }

    private LocalTime parseTime(Object timeObj) {
        if (timeObj == null) return null;
        if (timeObj instanceof LocalTime) {
            return (LocalTime) timeObj;
        }
        String s = String.valueOf(timeObj).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return null;
        try {
            if (s.toUpperCase().endsWith("AM") || s.toUpperCase().endsWith("PM")) {
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.ENGLISH);
                return LocalTime.parse(s, dtf);
            }
            if (s.length() == 5) {
                return LocalTime.parse(s);
            }
            if (s.length() > 5 && s.contains(":")) {
                return LocalTime.parse(s.substring(0, 5));
            }
        } catch (Exception e) {
            log.warn("Failed to parse time object: {}", timeObj);
        }
        return null;
    }

    private double calculateRequiredShiftHours(com.webicSoftIndia.HRMS.Roster.EmployeeRosterService.RosterScheduleResolved resolved) {
        if (resolved == null) return 8.0;
        Shift s = resolved.getShift();
        if (s != null && s.getWorkingHours() != null && s.getWorkingHours().doubleValue() > 0) {
            return s.getWorkingHours().doubleValue();
        }
        LocalTime start = s != null ? s.getStartTime() : null;
        LocalTime end = s != null ? s.getEndTime() : null;
        if (start == null || end == null) {
            if (resolved.getShiftTiming() != null && resolved.getShiftTiming().contains("-")) {
                String[] parts = resolved.getShiftTiming().split("-");
                if (parts.length == 2) {
                    start = parseTime(parts[0].trim());
                    end = parseTime(parts[1].trim());
                }
            }
        }
        if (start == null || end == null) return 8.0;

        long shiftMinutes = Duration.between(start, end).toMinutes();
        if (shiftMinutes < 0) shiftMinutes += 24 * 60;

        long breakMins = 60;
        if (s != null && s.getBreakStartTime() != null && s.getBreakEndTime() != null) {
            long bm = Duration.between(s.getBreakStartTime(), s.getBreakEndTime()).toMinutes();
            if (bm > 0) breakMins = bm;
        }

        double netMinutes = Math.max(0, shiftMinutes - breakMins);
        return BigDecimal.valueOf(netMinutes / 60.0).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private void sendAttendanceManagerNotification(
            Employee employee,
            Attendance attendance,
            com.webicSoftIndia.HRMS.Roster.EmployeeRosterService.RosterScheduleResolved resolved,
            String actionType
    ) {
        if (employee == null || attendance == null) return;

        String empName = ((employee.getFirstName() != null ? employee.getFirstName() : "") + " " +
                         (employee.getLastName() != null ? employee.getLastName() : "")).trim();
        String empCode = employee.getEmployeeCode() != null ? employee.getEmployeeCode() : ("EMP-" + employee.getId());

        String shiftName = resolved != null && resolved.getShiftName() != null ? resolved.getShiftName() : "General Shift";
        String shiftStartStr = resolved != null && resolved.getShift() != null && resolved.getShift().getStartTime() != null
                ? resolved.getShift().getStartTime().toString()
                : (resolved != null && resolved.getShiftTiming() != null ? resolved.getShiftTiming() : "09:30 AM");
        String shiftEndStr = resolved != null && resolved.getShift() != null && resolved.getShift().getEndTime() != null
                ? resolved.getShift().getEndTime().toString()
                : "06:30 PM";

        double requiredHours = calculateRequiredShiftHours(resolved);

        LocalTime inTime = attendance.getInTime();
        LocalTime outTime = attendance.getOutTime();
        BigDecimal totalHoursBD = attendance.getTotalHours();
        double totalHours = totalHoursBD != null ? totalHoursBD.doubleValue() : 0.0;

        LocalTime expectedStart = resolved != null && resolved.getShift() != null ? resolved.getShift().getStartTime() : parseTime(shiftStartStr);
        LocalTime expectedEnd = resolved != null && resolved.getShift() != null ? resolved.getShift().getEndTime() : parseTime(shiftEndStr);

        List<String> alerts = new ArrayList<>();
        if (inTime != null && expectedStart != null) {
            if (inTime.isBefore(expectedStart.minusMinutes(5))) {
                long earlyMins = Duration.between(inTime, expectedStart).toMinutes();
                alerts.add("Arrived early by " + earlyMins + " mins (In: " + inTime + ", Shift: " + shiftStartStr + ")");
            } else if (inTime.isAfter(expectedStart.plusMinutes(15))) {
                long lateMins = Duration.between(expectedStart, inTime).toMinutes();
                alerts.add("Arrived late by " + lateMins + " mins (In: " + inTime + ", Shift: " + shiftStartStr + ")");
            }
        }

        if ("CHECK_OUT".equals(actionType) && outTime != null && expectedEnd != null) {
            if (outTime.isBefore(expectedEnd.minusMinutes(5))) {
                long earlyGoMins = Duration.between(outTime, expectedEnd).toMinutes();
                alerts.add("Left early by " + earlyGoMins + " mins (Out: " + outTime + ", Shift: " + shiftEndStr + ")");
            }
            if (totalHours < requiredHours) {
                double shortfall = BigDecimal.valueOf(requiredHours - totalHours).setScale(2, RoundingMode.HALF_UP).doubleValue();
                alerts.add("Working hours shortfall: " + shortfall + " hrs (Worked: " + totalHours + " hrs, Required: " + requiredHours + " hrs)");
            }
        }

        StringBuilder msg = new StringBuilder();
        msg.append("Employee ").append(empName).append(" (").append(empCode).append(") marked ")
           .append("CHECK_IN".equals(actionType) ? "Check-In" : "Check-Out").append(" for ").append(attendance.getAttendanceDate()).append(". ")
           .append("Assigned Shift: ").append(shiftName).append(" (").append(shiftStartStr).append(" - ").append(shiftEndStr).append("). ")
           .append("In Time: ").append(inTime != null ? inTime : "N/A").append(", Out Time: ").append(outTime != null ? outTime : "N/A").append(". ");

        if (totalHours > 0) {
            msg.append("Total Worked: ").append(totalHours).append(" hrs (Required: ").append(requiredHours).append(" hrs). ");
        }

        if (!alerts.isEmpty()) {
            msg.append("⚠️ Alerts: ").append(String.join("; ", alerts)).append(".");
        } else {
            msg.append("Status: Present & Working.");
        }

        String title = "⏰ Attendance Alert: " + empName + " (" + actionType + ")";
        String finalMsg = msg.toString();

        Set<Long> managerIds = new HashSet<>();
        if (employee.getReportingManagerId() != null && employee.getReportingManagerId() > 0) {
            managerIds.add(employee.getReportingManagerId());
        }
        if (employee.getWorkingManagerId() != null && employee.getWorkingManagerId() > 0) {
            managerIds.add(employee.getWorkingManagerId());
        }
        if (employee.getManagerId() != null && employee.getManagerId() > 0) {
            managerIds.add(employee.getManagerId());
        }

        for (Long mgrId : managerIds) {
            if (mgrId.equals(employee.getId())) continue;
            Employee mgr = employeeRepository.findById(mgrId).orElse(null);
            if (mgr != null) {
                Notification notification = Notification.builder()
                        .organization(employee.getOrganization())
                        .unit(employee.getUnit())
                        .recipient(mgr)
                        .title(title)
                        .message(finalMsg)
                        .type("ATTENDANCE_ALERT")
                        .scope("PRIVATE")
                        .category("ATTENDANCE")
                        .isRead(false)
                        .status("UNREAD")
                        .isActive(true)
                        .build();
                notificationRepository.save(notification);
                log.info("Sent attendance manager notification to Manager ID {} for Employee ID {}", mgrId, employee.getId());
            }
        }
    }

    private Long resolveEmployeeId(Long providedEmpId, String sessionId) {
        if (providedEmpId != null && providedEmpId > 0) {
            return providedEmpId;
        }
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
            if (sessionData != null) {
                return sessionData.getId();
            }
        }
        return null;
    }
}
