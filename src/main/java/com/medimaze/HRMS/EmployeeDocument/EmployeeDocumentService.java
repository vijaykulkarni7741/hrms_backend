package com.medimaze.HRMS.EmployeeDocument;

import com.medimaze.HRMS.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeDocumentService {

    private final EmployeeDocumentRepository employeeDocumentRepository;

    @Transactional
    public ApiResponse<Void> createEmployeeDocument(EmployeeDocument document) {
        if (document.getEmployee() == null || document.getEmployee().getId() == null) {
            return ApiResponse.error("Employee ID is required for document");
        }

        if (document.getIsActive() == null) {
            document.setIsActive(true);
        }

        employeeDocumentRepository.save(document);
        log.info("Successfully created document for employee ID: {}", document.getEmployee().getId());
        return ApiResponse.success("Employee document created successfully", null);
    }
}
