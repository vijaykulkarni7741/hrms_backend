package com.medimaze.HRMS.Employee;

import com.medimaze.HRMS.Employee.dto.CreateEmployeeRequest;
import com.medimaze.HRMS.common.ApiResponse;
import com.medimaze.HRMS.common.IdRequest;
import com.medimaze.HRMS.common.PageRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/employees", "/v1/employees", "/api/v1/employee", "/v1/employee", "/employees", "/employee"})
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping({"/list", "/get-all"})
    public ResponseEntity<ApiResponse<List<Employee>>> listEmployees(
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam,
            @RequestBody(required = false) PageRequestDto pageRequest) {

        String targetSessionId = null;
        if (xSessionId != null && !xSessionId.trim().isEmpty()) {
            targetSessionId = xSessionId.trim();
        } else if (sessionIdHeader != null && !sessionIdHeader.trim().isEmpty()) {
            targetSessionId = sessionIdHeader.trim();
        } else if (sessionIdParam != null && !sessionIdParam.trim().isEmpty()) {
            targetSessionId = sessionIdParam.trim();
        }

        return ResponseEntity.ok(employeeService.getEmployeesBySessionUnit(targetSessionId));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createEmployee(@RequestBody CreateEmployeeRequest request) {
        ApiResponse<Void> response = employeeService.createEmployee(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<ApiResponse<Void>> updateEmployee(@RequestBody CreateEmployeeRequest request) {
        ApiResponse<Void> response = employeeService.createEmployee(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping({"/update-demographics", "/update-profile", "/demographics"})
    public ResponseEntity<ApiResponse<Employee>> updateEmployeeDemographics(@RequestBody java.util.Map<String, Object> payload) {
        ApiResponse<Employee> response = employeeService.updateEmployeeDemographics(payload);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/toggle-active")
    public ResponseEntity<ApiResponse<Employee>> toggleActiveStatus(@RequestBody IdRequest request) {
        Long id = request != null ? request.getId() : null;
        ApiResponse<Employee> response = employeeService.toggleActiveStatus(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@RequestBody IdRequest request) {
        Long id = request != null ? request.getId() : null;
        ApiResponse<Void> response = employeeService.deleteEmployee(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping({"/assign-shift-weekoff", "/update-shift-weekoff"})
    public ResponseEntity<ApiResponse<Employee>> assignShiftAndWeekOff(@RequestBody java.util.Map<String, Object> payload) {
        Long employeeId = payload.get("employeeId") != null ? Long.valueOf(payload.get("employeeId").toString()) : null;
        Long shiftId = payload.get("shiftId") != null ? Long.valueOf(payload.get("shiftId").toString()) : null;
        String weekOffDay1 = payload.get("weekOffDay1") != null ? payload.get("weekOffDay1").toString() : null;
        String weekOffDay2 = payload.get("weekOffDay2") != null ? payload.get("weekOffDay2").toString() : null;

        ApiResponse<Employee> response = employeeService.assignShiftAndWeekOff(employeeId, shiftId, weekOffDay1, weekOffDay2);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}

