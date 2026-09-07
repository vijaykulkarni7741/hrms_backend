package com.webicSoftIndia.HRMS.Department;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.common.IdRequest;
import com.webicSoftIndia.HRMS.common.PageRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/departments", "/v1/departments", "/api/v1/department", "/v1/department"})
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping({"/create", "/save", "/update"})
    public ResponseEntity<ApiResponse<Department>> createOrUpdateDepartment(@RequestBody Department department) {
        ApiResponse<Department> response = departmentService.saveOrUpdateDepartment(department);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@RequestBody IdRequest request) {
        Long id = request != null ? request.getId() : null;
        if (id != null) {
            departmentService.deleteDepartment(id);
            return ResponseEntity.ok(ApiResponse.success("Department deleted successfully", null));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("Valid ID is required"));
    }

    @PostMapping("/toggle-active")
    public ResponseEntity<ApiResponse<Department>> toggleActiveStatus(@RequestBody IdRequest request) {
        Long id = request != null ? request.getId() : null;
        ApiResponse<Department> response = departmentService.toggleActiveStatus(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping({"/list", "/get-all"})
    public ResponseEntity<ApiResponse<List<Department>>> listDepartments(
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam,
            @RequestBody(required = false) PageRequestDto pageRequest) {

        String targetSessionId = null;
        if (xSessionId != null && !xSessionId.trim().isEmpty()) {
            targetSessionId = xSessionId.trim();
        } else if (sessionIdHeader != null && !sessionIdHeader.trim().isEmpty()) {
            targetSessionId = sessionIdHeader.trim();
        } else if (sessionIdParam != null && !sessionIdParam.trim().isEmpty()) {
            targetSessionId = sessionIdParam.trim();
        }

        ApiResponse<List<Department>> response = departmentService.getDepartmentsBySessionUnit(targetSessionId);
        return ResponseEntity.ok(response);
    }
}
