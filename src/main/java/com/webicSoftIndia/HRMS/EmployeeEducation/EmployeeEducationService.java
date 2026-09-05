package com.webicSoftIndia.HRMS.EmployeeEducation;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeEducationService {

    private final EmployeeEducationRepository employeeEducationRepository;

    @Transactional
    public ApiResponse<Void> createEmployeeEducation(EmployeeEducation education) {
        if (education.getEmployee() == null || education.getEmployee().getId() == null) {
            return ApiResponse.error("Employee ID is required for education");
        }

        if (education.getIsActive() == null) {
            education.setIsActive(true);
        }

        employeeEducationRepository.save(education);
        log.info("Successfully created education record for employee ID: {}", education.getEmployee().getId());
        return ApiResponse.success("Employee education created successfully", null);
    }
}
