package com.medimaze.HRMS.EmployeeExperience;

import com.medimaze.HRMS.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employee-experiences")
@RequiredArgsConstructor
public class EmployeeExperienceController {

    private final EmployeeExperienceService employeeExperienceService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createEmployeeExperience(@RequestBody EmployeeExperience experience) {
        ApiResponse<Void> response = employeeExperienceService.createEmployeeExperience(experience);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
