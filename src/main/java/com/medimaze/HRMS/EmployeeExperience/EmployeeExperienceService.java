package com.medimaze.HRMS.EmployeeExperience;

import com.medimaze.HRMS.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeExperienceService {

    private final EmployeeExperienceRepository employeeExperienceRepository;

    @Transactional
    public ApiResponse<Void> createEmployeeExperience(EmployeeExperience experience) {
        if (experience.getEmployee() == null || experience.getEmployee().getId() == null) {
            return ApiResponse.error("Employee ID is required for experience");
        }

        if (experience.getIsActive() == null) {
            experience.setIsActive(true);
        }

        employeeExperienceRepository.save(experience);
        log.info("Successfully created experience record for employee ID: {}", experience.getEmployee().getId());
        return ApiResponse.success("Employee experience created successfully", null);
    }
}
