package com.webicSoftIndia.HRMS.EmployeeContact;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employee-contacts")
@RequiredArgsConstructor
public class EmployeeContactController {

    private final EmployeeContactService employeeContactService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createEmployeeContact(@RequestBody EmployeeContact contact) {
        ApiResponse<Void> response = employeeContactService.createEmployeeContact(contact);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
