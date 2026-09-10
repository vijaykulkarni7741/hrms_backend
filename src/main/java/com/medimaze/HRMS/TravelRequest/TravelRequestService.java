package com.medimaze.HRMS.TravelRequest;

import com.medimaze.HRMS.Employee.dto.EmployeeDto;
import com.medimaze.HRMS.common.ApiResponse;
import com.medimaze.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TravelRequestService {

    private final TravelRequestRepository travelRequestRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<TravelRequest> saveOrUpdateTravelRequest(TravelRequest request) {
        if (request == null) {
            return ApiResponse.error("TravelRequest payload cannot be null");
        }

        if (request.getId() != null && request.getId() > 0) {
            TravelRequest existing = travelRequestRepository.findById(request.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("TravelRequest record not found with ID: " + request.getId());
            }

            if (request.getDestination() != null) existing.setDestination(request.getDestination());
            if (request.getPurpose() != null) existing.setPurpose(request.getPurpose());
            if (request.getStartDate() != null) existing.setStartDate(request.getStartDate());
            if (request.getEndDate() != null) existing.setEndDate(request.getEndDate());
            if (request.getEstimatedBudget() != null) existing.setEstimatedBudget(request.getEstimatedBudget());
            if (request.getAdvanceRequested() != null) existing.setAdvanceRequested(request.getAdvanceRequested());
            if (request.getStatus() != null) existing.setStatus(request.getStatus());
            if (request.getIsActive() != null) existing.setIsActive(request.getIsActive());

            TravelRequest updated = travelRequestRepository.save(existing);
            return ApiResponse.success("TravelRequest updated successfully", updated);
        }

        if (request.getDestination() == null || request.getDestination().trim().isEmpty()) {
            return ApiResponse.error("Destination is required");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return ApiResponse.error("Start date and end date are required");
        }

        if (request.getStatus() == null) request.setStatus("PENDING");
        if (request.getIsActive() == null) request.setIsActive(true);

        TravelRequest saved = travelRequestRepository.save(request);
        return ApiResponse.success("TravelRequest created successfully", saved);
    }

    @Transactional
    public ApiResponse<TravelRequest> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid TravelRequest ID is required");
        }
        TravelRequest request = travelRequestRepository.findById(id).orElse(null);
        if (request == null) {
            return ApiResponse.error("TravelRequest record not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(request.getIsActive());
        request.setIsActive(newStatus);
        request.setStatus(newStatus ? "PENDING" : "INACTIVE");

        TravelRequest updated = travelRequestRepository.save(request);
        return ApiResponse.success("TravelRequest active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<TravelRequest>> getTravelRequestsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<TravelRequest> list;
        if (unitId != null && unitId > 0) {
            list = travelRequestRepository.findByUnitId(unitId);
        } else {
            list = travelRequestRepository.findAll();
        }
        return ApiResponse.success("TravelRequests retrieved successfully", list);
    }
}
