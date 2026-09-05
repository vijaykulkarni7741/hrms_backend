package com.webicSoftIndia.HRMS.EmployeeAddress;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employee-addresses")
@RequiredArgsConstructor
public class EmployeeAddressController {

    private final EmployeeAddressService employeeAddressService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createEmployeeAddress(@RequestBody EmployeeAddress address) {
        ApiResponse<Void> response = employeeAddressService.createEmployeeAddress(address);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
