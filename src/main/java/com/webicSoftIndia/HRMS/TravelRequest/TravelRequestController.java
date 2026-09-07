package com.webicSoftIndia.HRMS.TravelRequest;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.common.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({
    "/api/v1/travel-requests", "/v1/travel-requests", 
    "/api/v1/travel", "/v1/travel",
    "/api/v1/travel/approvals", "/v1/travel/approvals",
    "/api/v1/travel-approvals", "/v1/travel-approvals"
})
@RequiredArgsConstructor
public class TravelRequestController {

    private final TravelRequestService travelRequestService;

    @PostMapping({"/create", "/save", "/update", "/add"})
    public ResponseEntity<ApiResponse<TravelRequest>> createOrUpdateTravelRequest(@RequestBody TravelRequest requestPayload) {
        ApiResponse<TravelRequest> response = travelRequestService.saveOrUpdateTravelRequest(requestPayload);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/toggle-active")
    public ResponseEntity<ApiResponse<TravelRequest>> toggleActiveStatus(@RequestBody IdRequest request) {
        Long id = request != null ? request.getId() : null;
        ApiResponse<TravelRequest> response = travelRequestService.toggleActiveStatus(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping({"/list", "/approvals", "/all"})
    public ResponseEntity<ApiResponse<List<TravelRequest>>> listTravelRequests(
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

        ApiResponse<List<TravelRequest>> response = travelRequestService.getTravelRequestsBySessionUnit(targetSessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
