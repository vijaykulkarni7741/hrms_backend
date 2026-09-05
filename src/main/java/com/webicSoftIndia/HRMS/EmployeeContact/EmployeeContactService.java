package com.webicSoftIndia.HRMS.EmployeeContact;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeContactService {

    private final EmployeeContactRepository employeeContactRepository;

    @Transactional
    public ApiResponse<Void> createEmployeeContact(EmployeeContact contact) {
        if (contact.getEmployee() == null || contact.getEmployee().getId() == null) {
            return ApiResponse.error("Employee ID is required for contact");
        }

        if (contact.getIsActive() == null) {
            contact.setIsActive(true);
        }

        employeeContactRepository.save(contact);
        log.info("Successfully created contact for employee ID: {}", contact.getEmployee().getId());
        return ApiResponse.success("Employee contact created successfully", null);
    }
}
