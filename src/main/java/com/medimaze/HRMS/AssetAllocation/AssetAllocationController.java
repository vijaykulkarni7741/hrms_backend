package com.medimaze.HRMS.AssetAllocation;

import com.medimaze.HRMS.common.ApiResponse;
import com.medimaze.HRMS.common.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({
        "/api/v1/asset-allocations",
        "/v1/asset-allocations",
        "/api/v1/assets",
        "/v1/assets",
        "/asset-allocations",
        "/assets"
})
@RequiredArgsConstructor
public class AssetAllocationController {

    private final AssetAllocationService assetAllocationService;

    @PostMapping({"/create", "/save", "/allocate"})
    public ResponseEntity<ApiResponse<AssetAllocation>> createOrUpdateAssetAllocation(
            @RequestBody Map<String, Object> payload,
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader) {

        String sessionId = xSessionId != null ? xSessionId : sessionIdHeader;
        ApiResponse<AssetAllocation> response = assetAllocationService.createOrUpdateFromMap(payload, sessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/toggle-active")
    public ResponseEntity<ApiResponse<AssetAllocation>> toggleActiveStatus(@RequestBody IdRequest request) {
        Long id = request != null ? request.getId() : null;
        ApiResponse<AssetAllocation> response = assetAllocationService.toggleActiveStatus(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping({"/list", "/all"})
    public ResponseEntity<ApiResponse<List<AssetAllocation>>> listAssetAllocations(
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader) {

        String targetSessionId = xSessionId != null ? xSessionId : sessionIdHeader;
        ApiResponse<List<AssetAllocation>> response = assetAllocationService.getAssetAllocationsBySessionUnit(targetSessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping({"/my-assets", "/employee-assets"})
    public ResponseEntity<ApiResponse<List<AssetAllocation>>> getEmployeeAssets(
            @RequestBody(required = false) Map<String, Object> payload,
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader) {

        Long empId = null;
        if (payload != null && payload.get("employeeId") != null) {
            try {
                empId = Long.valueOf(payload.get("employeeId").toString());
            } catch (Exception ignored) {}
        }

        String targetSessionId = xSessionId != null ? xSessionId : sessionIdHeader;
        ApiResponse<List<AssetAllocation>> response = assetAllocationService.getAssetAllocationsByEmployee(empId, targetSessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
