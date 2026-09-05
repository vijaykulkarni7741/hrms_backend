package com.webicSoftIndia.HRMS.EmployeeDocument;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employee-documents")
@RequiredArgsConstructor
public class EmployeeDocumentController {

    private final EmployeeDocumentService employeeDocumentService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Void>> createEmployeeDocument(@RequestBody EmployeeDocument document) {
        ApiResponse<Void> response = employeeDocumentService.createEmployeeDocument(document);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
