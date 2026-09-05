package com.webicSoftIndia.HRMS.EmployeeAddress;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeAddressService {

    private final EmployeeAddressRepository employeeAddressRepository;

    @Transactional
    public ApiResponse<Void> createEmployeeAddress(EmployeeAddress address) {
        if (address.getEmployee() == null || address.getEmployee().getId() == null) {
            return ApiResponse.error("Employee ID is required for address");
        }

        if (address.getIsActive() == null) {
            address.setIsActive(true);
        }

        employeeAddressRepository.save(address);
        log.info("Successfully created address for employee ID: {}", address.getEmployee().getId());
        return ApiResponse.success("Employee address created successfully", null);
    }
}
