package com.webicSoftIndia.HRMS.AdvanceSalary;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.common.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/advance-salaries")
@RequiredArgsConstructor
public class AdvanceSalaryController {

    private final AdvanceSalaryService advanceSalaryService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AdvanceSalary>> createOrUpdateAdvanceSalary(@RequestBody AdvanceSalary advance) {
        ApiResponse<AdvanceSalary> response = advanceSalaryService.saveOrUpdateAdvanceSalary(advance);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/toggle-active")
    public ResponseEntity<ApiResponse<AdvanceSalary>> toggleActiveStatus(@RequestBody IdRequest request) {
        Long id = request != null ? request.getId() : null;
        ApiResponse<AdvanceSalary> response = advanceSalaryService.toggleActiveStatus(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<AdvanceSalary>>> listAdvanceSalaries(
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {

        String targetSessionId = null;
        if (xSessionId != null && !xSessionId.trim().isEmpty()) {
            targetSessionId = xSessionId.trim();
        } else if (sessionIdHeader != null && !sessionIdHeader.trim().isEmpty()) {
            targetSessionId = sessionIdHeader.trim();
        } else if (sessionIdParam != null && !sessionIdParam.trim().isEmpty()) {
            targetSessionId = sessionIdParam.trim();
        }

        ApiResponse<List<AdvanceSalary>> response = advanceSalaryService.getAdvanceSalariesBySessionUnit(targetSessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
