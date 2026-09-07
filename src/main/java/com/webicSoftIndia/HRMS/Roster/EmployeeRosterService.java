package com.webicSoftIndia.HRMS.Roster;

import com.webicSoftIndia.HRMS.Employee.Employee;
import com.webicSoftIndia.HRMS.Employee.EmployeeRepository;
import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.Shift.Shift;
import com.webicSoftIndia.HRMS.Shift.ShiftRepository;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class EmployeeRosterService {

    private final EmployeeRosterRepository rosterRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final RedisSessionService redisSessionService;

    public EmployeeRosterService(EmployeeRosterRepository rosterRepository,
                                 EmployeeRepository employeeRepository,
                                 ShiftRepository shiftRepository,
                                 RedisSessionService redisSessionService) {
        this.rosterRepository = rosterRepository;
        this.employeeRepository = employeeRepository;
        this.shiftRepository = shiftRepository;
        this.redisSessionService = redisSessionService;
    }

    @Transactional
    public ApiResponse<List<EmployeeRoster>> createRoster(CreateRosterRequest request) {
        if (request == null) {
            return ApiResponse.error("Request payload cannot be null");
        }

        List<Long> empIds = request.getEmployeeIds();
        if (empIds == null || empIds.isEmpty()) {
            if (request.getEmployeeId() != null && request.getEmployeeId() > 0) {
                empIds = List.of(request.getEmployeeId());
            } else {
                return ApiResponse.error("At least one employee must be selected for roster assignment");
            }
        }

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        String rosterType = (request.getRosterType() != null && !request.getRosterType().trim().isEmpty())
                ? request.getRosterType().trim().toUpperCase() : "WEEKLY";

        LocalDate endDate = request.getEndDate();
        if (endDate == null) {
            switch (rosterType) {
                case "THREE_MONTHS":
                    endDate = startDate.plusMonths(3).minusDays(1);
                    break;
                case "MONTHLY":
                    endDate = startDate.plusMonths(1).minusDays(1);
                    break;
                case "WEEKLY":
                default:
                    endDate = startDate.plusDays(6);
                    break;
            }
        }

        Shift shift = null;
        if (request.getShiftId() != null && request.getShiftId() > 0) {
            shift = shiftRepository.findById(request.getShiftId()).orElse(null);
        }

        String w1 = request.getWeekOffDay1() != null ? request.getWeekOffDay1().trim().toUpperCase() : "SATURDAY";
        String w2 = request.getWeekOffDay2() != null ? request.getWeekOffDay2().trim().toUpperCase() : "SUNDAY";
        Boolean isAutoRollover = request.getIsAutoRollover() != null ? request.getIsAutoRollover() : true;

        List<EmployeeRoster> createdList = new ArrayList<>();

        for (Long empId : empIds) {
            Employee emp = employeeRepository.findById(empId).orElse(null);
            if (emp == null) continue;

            EmployeeRoster roster = EmployeeRoster.builder()
                    .employee(emp)
                    .organization(emp.getOrganization())
                    .unit(emp.getUnit())
                    .shift(shift)
                    .weekOffDay1(w1)
                    .weekOffDay2(w2)
                    .rosterType(rosterType)
                    .startDate(startDate)
                    .endDate(endDate)
                    .isAutoRollover(isAutoRollover)
                    .isActive(true)
                    .remarks(request.getRemarks())
                    .build();

            EmployeeRoster saved = rosterRepository.save(roster);
            createdList.add(saved);

            // Update Employee default configuration
            if (shift != null) emp.setShiftId(shift.getId());
            emp.setWeekOffDay1(w1);
            emp.setWeekOffDay2(w2);
            employeeRepository.save(emp);
        }

        log.info("Successfully created {} roster entries for rosterType {} from {} to {}", createdList.size(), rosterType, startDate, endDate);
        return ApiResponse.success("Roster schedule created successfully for " + createdList.size() + " employee(s)", createdList);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Map<String, Object>>> getHRRosterSummaryList() {
        List<Employee> employees = employeeRepository.findAll();
        List<Shift> allShifts = shiftRepository.findAll();
        Map<Long, Shift> shiftMap = new HashMap<>();
        for (Shift s : allShifts) {
            shiftMap.put(s.getId(), s);
        }

        LocalDate today = LocalDate.now();
        List<Map<String, Object>> summaryList = new ArrayList<>();

        for (Employee emp : employees) {
            Map<String, Object> map = new HashMap<>();
            map.put("employeeId", emp.getId());
            map.put("employeeCode", emp.getEmployeeCode());
            map.put("employeeName", emp.getFirstName() + " " + (emp.getLastName() != null ? emp.getLastName() : ""));
            map.put("department", emp.getDepartment() != null ? emp.getDepartment().getDepartmentName() : "N/A");
            map.put("designation", emp.getDesignation() != null ? emp.getDesignation().getDesignationName() : "N/A");

            // Resolve dynamic roster info
            RosterScheduleResolved resolved = resolveEffectiveSchedule(emp, today, shiftMap);

            map.put("shiftId", resolved.getShiftId());
            map.put("shiftName", resolved.getShiftName());
            map.put("shiftTiming", resolved.getShiftTiming());
            map.put("weekOffDay1", resolved.getWeekOffDay1());
            map.put("weekOffDay2", resolved.getWeekOffDay2());
            map.put("rosterType", resolved.getRosterType());
            map.put("startDate", resolved.getStartDate());
            map.put("endDate", resolved.getEndDate());
            map.put("isAutoRollover", resolved.getIsAutoRollover());
            map.put("source", resolved.getSource());

            summaryList.add(map);
        }

        return ApiResponse.success("HR Employee Roster summary list retrieved successfully", summaryList);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<EmployeeRoster>> getAllRosters() {
        List<EmployeeRoster> list = rosterRepository.findByIsActiveTrueOrderByStartDateDesc();
        return ApiResponse.success("All roster schedules retrieved successfully", list);
    }

    @Transactional
    public ApiResponse<String> deleteRoster(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Roster ID is required");
        }
        Optional<EmployeeRoster> opt = rosterRepository.findById(id);
        if (opt.isPresent()) {
            EmployeeRoster roster = opt.get();
            roster.setIsActive(false);
            rosterRepository.save(roster);
            return ApiResponse.success("Roster schedule deleted/deactivated successfully", "DELETED");
        }
        return ApiResponse.error("Roster record not found");
    }

    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> getMyRoster(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID is required");
        }

        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session");
        }

        Long empId = sessionData.getId();
        if (empId == null || empId <= 0) {
            return ApiResponse.error("Employee ID could not be resolved from session");
        }

        Employee employee = employeeRepository.findById(empId).orElse(null);
        if (employee == null) {
            return ApiResponse.error("Employee record not found");
        }

        LocalDate today = LocalDate.now();
        RosterScheduleResolved resolved = resolveEffectiveSchedule(employee, today, null);
        Shift shift = resolved != null ? resolved.getShift() : null;

        // Build response map with all useful shift + roster info
        Map<String, Object> result = new LinkedHashMap<>();

        // ── Shift Info ────────────────────────────────────────────
        result.put("shiftId", resolved != null ? resolved.getShiftId() : null);
        result.put("shiftName", resolved != null ? resolved.getShiftName() : "General Day Shift");
        result.put("shiftTiming", resolved != null ? resolved.getShiftTiming() : "09:30 - 18:30");
        result.put("startTime", shift != null ? shift.getStartTime() : null);
        result.put("endTime", shift != null ? shift.getEndTime() : null);
        result.put("shiftType", shift != null ? shift.getShiftType() : null);
        result.put("workingHours", shift != null ? shift.getWorkingHours() : null);
        result.put("minimumWorkingHours", shift != null ? shift.getMinimumWorkingHours() : null);
        result.put("graceTime", shift != null ? shift.getGraceTime() : null);
        result.put("lateMarkAfter", shift != null ? shift.getLateMarkAfter() : null);
        result.put("halfDayHours", shift != null ? shift.getHalfDayHours() : null);
        result.put("breakStartTime", shift != null ? shift.getBreakStartTime() : null);
        result.put("breakEndTime", shift != null ? shift.getBreakEndTime() : null);
        result.put("nightShift", shift != null ? Boolean.TRUE.equals(shift.getNightShift()) : false);
        result.put("crossDayShift", shift != null ? Boolean.TRUE.equals(shift.getCrossDayShift()) : false);
        result.put("overtimeAllowed", shift != null ? Boolean.TRUE.equals(shift.getOvertimeAllowed()) : false);

        // ── Roster Info ───────────────────────────────────────────
        result.put("weekOffDay1", resolved != null ? resolved.getWeekOffDay1() : "SATURDAY");
        result.put("weekOffDay2", resolved != null ? resolved.getWeekOffDay2() : "SUNDAY");
        result.put("rosterType", resolved != null ? resolved.getRosterType() : "DEFAULT");
        result.put("rosterStartDate", resolved != null ? resolved.getStartDate() : null);
        result.put("rosterEndDate", resolved != null ? resolved.getEndDate() : null);
        result.put("isAutoRollover", resolved != null ? resolved.getIsAutoRollover() : true);
        result.put("resolvedFrom", resolved != null ? resolved.getSource() : "DEFAULT");
        result.put("resolvedForDate", today.toString());

        log.info("Employee ID {} roster resolved for today {} from source: {}",
                empId, today, resolved != null ? resolved.getSource() : "N/A");

        return ApiResponse.success("Current shift and roster resolved successfully", result);
    }

    public RosterScheduleResolved resolveEffectiveSchedule(Employee emp, LocalDate targetDate, Map<Long, Shift> shiftMap) {
        if (emp == null) {
            return RosterScheduleResolved.builder()
                    .weekOffDay1("SATURDAY")
                    .weekOffDay2("SUNDAY")
                    .rosterType("DEFAULT")
                    .isAutoRollover(true)
                    .source("DEFAULT")
                    .build();
        }

        LocalDate queryDate = targetDate != null ? targetDate : LocalDate.now();

        // 1. Check exact roster for queryDate
        List<EmployeeRoster> exact = rosterRepository.findExactRosterForDate(emp.getId(), queryDate);
        if (!exact.isEmpty()) {
            EmployeeRoster r = exact.get(0);
            return buildFromRoster(r, "EXACT_ROSTER");
        }

        // 2. Check auto-rollover roster prior to queryDate
        List<EmployeeRoster> rollover = rosterRepository.findAutoRolloverRostersPriorToDate(emp.getId(), queryDate);
        if (!rollover.isEmpty()) {
            EmployeeRoster r = rollover.get(0);
            return buildFromRoster(r, "AUTO_ROLLOVER");
        }

        // 3. Fallback to Employee profile default
        String w1 = emp.getWeekOffDay1() != null ? emp.getWeekOffDay1() : "SATURDAY";
        String w2 = emp.getWeekOffDay2() != null ? emp.getWeekOffDay2() : "SUNDAY";
        Long sId = emp.getShiftId();
        Shift s = (sId != null && shiftMap != null) ? shiftMap.get(sId) : null;
        if (s == null && sId != null) {
            s = shiftRepository.findById(sId).orElse(null);
        }

        return RosterScheduleResolved.builder()
                .shiftId(sId)
                .shift(s)
                .shiftName(s != null ? s.getShiftName() : "General Day Shift")
                .shiftTiming(s != null ? (s.getStartTime() + " - " + s.getEndTime()) : "09:30 - 18:30")
                .weekOffDay1(w1)
                .weekOffDay2(w2)
                .rosterType("PROFILE_DEFAULT")
                .startDate(null)
                .endDate(null)
                .isAutoRollover(true)
                .source("EMPLOYEE_PROFILE")
                .build();
    }

    private RosterScheduleResolved buildFromRoster(EmployeeRoster r, String source) {
        Shift s = r.getShift();
        return RosterScheduleResolved.builder()
                .shiftId(s != null ? s.getId() : null)
                .shift(s)
                .shiftName(s != null ? s.getShiftName() : "General Day Shift")
                .shiftTiming(s != null ? (s.getStartTime() + " - " + s.getEndTime()) : "09:30 - 18:30")
                .weekOffDay1(r.getWeekOffDay1())
                .weekOffDay2(r.getWeekOffDay2())
                .rosterType(r.getRosterType())
                .startDate(r.getStartDate())
                .endDate(r.getEndDate())
                .isAutoRollover(Boolean.TRUE.equals(r.getIsAutoRollover()))
                .source(source)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRosterRequest {
        private Long employeeId;
        private List<Long> employeeIds;
        private Long shiftId;
        private String weekOffDay1;
        private String weekOffDay2;
        private String rosterType; // WEEKLY, MONTHLY, THREE_MONTHS
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isAutoRollover;
        private String remarks;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RosterScheduleResolved {
        private Long shiftId;
        private Shift shift;
        private String shiftName;
        private String shiftTiming;
        private String weekOffDay1;
        private String weekOffDay2;
        private String rosterType;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isAutoRollover;
        private String source;
    }
}
