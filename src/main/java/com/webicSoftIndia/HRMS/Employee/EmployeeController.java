package com.webicSoftIndia.HRMS.Employee;

import com.webicSoftIndia.HRMS.Employee.dto.CreateEmployeeRequest;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createEmployee(@RequestBody CreateEmployeeRequest request) {
        ApiResponse<Void> response = employeeService.createEmployee(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
