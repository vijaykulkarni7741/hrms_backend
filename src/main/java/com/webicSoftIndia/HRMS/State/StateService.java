package com.webicSoftIndia.HRMS.State;

import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StateService {

    private final StateRepository stateRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<State> saveOrUpdateState(State state) {
        if (state == null) {
            return ApiResponse.error("State payload cannot be null");
        }

        // UPDATE MODE
        if (state.getId() != null && state.getId() > 0) {
            State existing = stateRepository.findById(state.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("State not found with ID: " + state.getId());
            }

            if (state.getStateName() != null && !state.getStateName().trim().isEmpty()) {
                existing.setStateName(state.getStateName().trim());
            }
            if (state.getCountry() != null) {
                existing.setCountry(state.getCountry());
            }
            if (state.getStatus() != null) {
                existing.setStatus(state.getStatus());
            }
            if (state.getIsActive() != null) {
                existing.setIsActive(state.getIsActive());
            }

            State updated = stateRepository.save(existing);
            log.info("Updated State ID: {}", updated.getId());
            return ApiResponse.success("State updated successfully", updated);
        }

        // CREATE MODE
        if (state.getStateCode() == null || state.getStateCode().trim().isEmpty()) {
            return ApiResponse.error("State code is required");
        }
        if (state.getStateName() == null || state.getStateName().trim().isEmpty()) {
            return ApiResponse.error("State name is required");
        }

        if (stateRepository.findByStateCode(state.getStateCode().trim()).isPresent()) {
            return ApiResponse.error("State code already exists");
        }

        if (state.getStatus() == null) {
            state.setStatus("ACTIVE");
        }
        if (state.getIsActive() == null) {
            state.setIsActive(true);
        }

        State saved = stateRepository.save(state);
        log.info("Created State ID: {}", saved.getId());
        return ApiResponse.success("State created successfully", saved);
    }

    @Transactional
    public ApiResponse<State> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid State ID is required");
        }
        State state = stateRepository.findById(id).orElse(null);
        if (state == null) {
            return ApiResponse.error("State not found with ID: " + id);
        }

        boolean currentStatus = Boolean.TRUE.equals(state.getIsActive());
        boolean newStatus = !currentStatus;
        state.setIsActive(newStatus);
        state.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        State updated = stateRepository.save(state);
        log.info("Toggled active status for State ID: {} to {}", id, newStatus);
        return ApiResponse.success("State active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<State>> getStatesBySessionUnit(String sessionId) {
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
            if (sessionData != null && sessionData.getUnitId() != null && sessionData.getUnitId() > 0) {
                // Return states
            }
        }
        List<State> list = stateRepository.findAll();
        return ApiResponse.success("States retrieved successfully", list);
    }
}

