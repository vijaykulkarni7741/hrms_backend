package com.webicSoftIndia.HRMS.Dashboard;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping({"/api/v1/dashboard", "/v1/dashboard"})
@RequiredArgsConstructor
public class DashboardController {

    @PostMapping("/hr-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHrStats() {
        Map<String, Object> data = new HashMap<>();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", 142);
        stats.put("presentToday", 118);
        stats.put("absentToday", 8);
        stats.put("onLeave", 12);
        stats.put("lateArrivals", 14);
        stats.put("earlyDepartures", 6);
        stats.put("onTimeArrivals", 104);
        stats.put("pendingLeaveApprovals", 7);
        stats.put("monthlyPayroll", 2450000);
        stats.put("newJoineesMonth", 6);
        stats.put("exitingMonth", 2);

        List<Map<String, Object>> pendingLeaves = new ArrayList<>();
        pendingLeaves.add(Map.of("id", 1, "initials", "RS", "name", "Rahul Sharma", "leaveType", "Casual Leave", "days", 2, "from", "05 Sep"));
        pendingLeaves.add(Map.of("id", 2, "initials", "PK", "name", "Priya Kapoor", "leaveType", "Sick Leave", "days", 1, "from", "06 Sep"));
        pendingLeaves.add(Map.of("id", 3, "initials", "AM", "name", "Amit Mehta", "leaveType", "Earned Leave", "days", 3, "from", "07 Sep"));
        pendingLeaves.add(Map.of("id", 4, "initials", "SJ", "name", "Sunita Joshi", "leaveType", "Casual Leave", "days", 1, "from", "08 Sep"));

        List<Map<String, Object>> deptStats = new ArrayList<>();
        deptStats.add(Map.of("dept", "Engineering", "count", 42, "pct", 85));
        deptStats.add(Map.of("dept", "Sales", "count", 28, "pct", 56));
        deptStats.add(Map.of("dept", "HR", "count", 12, "pct", 24));
        deptStats.add(Map.of("dept", "Finance", "count", 18, "pct", 36));
        deptStats.add(Map.of("dept", "Operations", "count", 22, "pct", 44));
        deptStats.add(Map.of("dept", "Support", "count", 20, "pct", 40));

        List<Map<String, Object>> empStatus = new ArrayList<>();
        empStatus.add(Map.of("icon", "🟢", "label", "Active", "count", 128, "badge", "badge-success"));
        empStatus.add(Map.of("icon", "🟡", "label", "Probation", "count", 9, "badge", "badge-warning"));
        empStatus.add(Map.of("icon", "🔵", "label", "Confirmed", "count", 119, "badge", "badge-info"));
        empStatus.add(Map.of("icon", "🔴", "label", "Resigned", "count", 5, "badge", "badge-danger"));
        empStatus.add(Map.of("icon", "⚪", "label", "Inactive", "count", 9, "badge", "badge-warning"));

        data.put("stats", stats);
        data.put("pendingLeaves", pendingLeaves);
        data.put("deptStats", deptStats);
        data.put("empStatus", empStatus);

        return ResponseEntity.ok(ApiResponse.success("HR stats retrieved successfully", data));
    }

    @PostMapping("/manager-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getManagerStats() {
        Map<String, Object> data = new HashMap<>();

        Map<String, Object> stats = new HashMap<>();
        stats.put("teamSize", 12);
        stats.put("teamPresent", 10);
        stats.put("teamAbsent", 1);
        stats.put("pendingLeaves", 3);
        stats.put("lateToday", 2);
        stats.put("earlyDepartures", 1);
        stats.put("overtimeCount", 3);
        stats.put("avgHours", 8.4);
        stats.put("earliestIn", "08:14");
        stats.put("latestIn", "10:32");

        List<Map<String, Object>> team = new ArrayList<>();
        team.add(Map.of("id", 1, "initials", "RS", "name", "Rahul Sharma", "designation", "Sr. Developer", "status", "PRESENT", "inTime", "09:05", "isLate", false, "isEarlyDep", false));
        team.add(Map.of("id", 2, "initials", "PK", "name", "Priya Kapoor", "designation", "Developer", "status", "PRESENT", "inTime", "10:32", "isLate", true, "isEarlyDep", false));
        team.add(Map.of("id", 3, "initials", "AM", "name", "Amit Mehta", "designation", "QA Engineer", "status", "LEAVE", "inTime", "", "isLate", false, "isEarlyDep", false));
        team.add(Map.of("id", 4, "initials", "SJ", "name", "Sunita Joshi", "designation", "UI Designer", "status", "PRESENT", "inTime", "08:55", "isLate", false, "isEarlyDep", true));

        List<Map<String, Object>> pendingLeaves = new ArrayList<>();
        pendingLeaves.add(Map.of("id", 1, "empName", "Rahul Sharma", "leaveType", "Casual Leave", "from", "07 Sep", "to", "08 Sep", "days", 2, "reason", "Family function"));
        pendingLeaves.add(Map.of("id", 2, "empName", "Neha Kapoor", "leaveType", "Sick Leave", "from", "06 Sep", "to", "06 Sep", "days", 1, "reason", "Not feeling well"));

        data.put("stats", stats);
        data.put("team", team);
        data.put("pendingLeaves", pendingLeaves);

        return ResponseEntity.ok(ApiResponse.success("Manager stats retrieved successfully", data));
    }

    @PostMapping("/employee-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmployeeStats() {
        Map<String, Object> data = new HashMap<>();

        Map<String, Object> stats = new HashMap<>();
        stats.put("todayStatus", "PRESENT");
        stats.put("checkInTime", "09:05");
        stats.put("checkOutTime", "");
        stats.put("hoursWorked", 7.5);
        stats.put("leaveBalance", 14);
        stats.put("attendedDays", 21);
        stats.put("payslipsCount", 8);
        stats.put("presentDays", 21);
        stats.put("absentDays", 1);
        stats.put("leaveTaken", 2);
        stats.put("lateDays", 2);
        stats.put("earlyDepartureDays", 1);
        stats.put("lastSalary", 55000);

        List<Map<String, Object>> leaveBalances = new ArrayList<>();
        leaveBalances.add(Map.of("type", "Casual Leave", "total", 12, "remaining", 10, "icon", "🌴", "color", "var(--success)"));
        leaveBalances.add(Map.of("type", "Sick Leave", "total", 8, "remaining", 7, "icon", "🏥", "color", "var(--danger)"));
        leaveBalances.add(Map.of("type", "Earned Leave", "total", 15, "remaining", 13, "icon", "✈️", "color", "var(--info)"));

        List<Map<String, Object>> myLeaves = new ArrayList<>();
        myLeaves.add(Map.of("id", 1, "leaveType", "Casual Leave", "from", "25 Aug", "to", "26 Aug", "days", 2, "status", "APPROVED", "icon", "✅", "remarks", ""));
        myLeaves.add(Map.of("id", 2, "leaveType", "Earned Leave", "from", "15 Sep", "to", "17 Sep", "days", 3, "status", "PENDING", "icon", "🌴", "remarks", ""));

        List<Map<String, Object>> holidays = new ArrayList<>();
        holidays.add(Map.of("day", "15", "month", "Aug", "name", "Independence Day", "type", "National Holiday"));
        holidays.add(Map.of("day", "02", "month", "Oct", "name", "Gandhi Jayanti", "type", "National Holiday"));
        holidays.add(Map.of("day", "24", "month", "Oct", "name", "Dussehra", "type", "Festival Holiday"));

        data.put("stats", stats);
        data.put("leaveBalances", leaveBalances);
        data.put("myLeaves", myLeaves);
        data.put("holidays", holidays);

        return ResponseEntity.ok(ApiResponse.success("Employee stats retrieved successfully", data));
    }

    @PostMapping("/ceo-stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCeoStats() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalEmployees", 142);
        data.put("presentToday", 118);
        data.put("onLeave", 12);
        data.put("absent", 8);
        data.put("remote", 4);
        data.put("pendingApprovals", 19);
        data.put("pendingLeaves", 7);
        data.put("pendingTravel", 5);
        data.put("pendingExpenses", 4);
        data.put("pendingWorkflows", 3);
        data.put("monthlyPayroll", 24500000);
        data.put("attendancePct", 83);
        data.put("totalDepartments", 8);
        data.put("attritionRate", 4.2);
        data.put("newJoinees", 6);
        data.put("exits", 2);
        data.put("onProbation", 9);

        return ResponseEntity.ok(ApiResponse.success("CEO stats retrieved successfully", data));
    }
}
