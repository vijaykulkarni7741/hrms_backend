package com.webicSoftIndia.HRMS.Designation;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.common.IdRequest;
import com.webicSoftIndia.HRMS.common.PageRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/designations", "/v1/designations", "/api/v1/designation", "/v1/designation"})
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService designationService;

    @PostMapping({"/create", "/save", "/update"})
    public ResponseEntity<ApiResponse<Designation>> createOrUpdateDesignation(@RequestBody Designation designation) {
        ApiResponse<Designation> response = designationService.saveOrUpdateDesignation(designation);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteDesignation(@RequestBody IdRequest request) {
        Long id = request != null ? request.getId() : null;
        if (id != null) {
            designationService.deleteDesignation(id);
            return ResponseEntity.ok(ApiResponse.success("Designation deleted successfully", null));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("Valid ID is required"));
    }

    @PostMapping("/toggle-active")
    public ResponseEntity<ApiResponse<Designation>> toggleActiveStatus(@RequestBody IdRequest request) {
        Long id = request != null ? request.getId() : null;
        ApiResponse<Designation> response = designationService.toggleActiveStatus(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping({"/list", "/get-all"})
    public ResponseEntity<ApiResponse<List<Designation>>> listDesignations(
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

        ApiResponse<List<Designation>> response = designationService.getDesignationsBySessionUnit(targetSessionId);
        return ResponseEntity.ok(response);
    }
}

