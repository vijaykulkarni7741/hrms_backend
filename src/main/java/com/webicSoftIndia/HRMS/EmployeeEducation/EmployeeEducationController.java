package com.webicSoftIndia.HRMS.EmployeeEducation;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employee-educations")
@RequiredArgsConstructor
public class EmployeeEducationController {

    private final EmployeeEducationService employeeEducationService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createEmployeeEducation(@RequestBody EmployeeEducation education) {
        ApiResponse<Void> response = employeeEducationService.createEmployeeEducation(education);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
