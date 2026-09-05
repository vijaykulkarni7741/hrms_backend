package com.webicSoftIndia.HRMS.HelpdeskTicket;

import com.webicSoftIndia.HRMS.Employee.dto.EmployeeDto;
import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HelpdeskTicketService {

    private final HelpdeskTicketRepository helpdeskTicketRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<HelpdeskTicket> saveOrUpdateHelpdeskTicket(HelpdeskTicket ticket) {
        if (ticket == null) {
            return ApiResponse.error("HelpdeskTicket payload cannot be null");
        }

        if (ticket.getId() != null && ticket.getId() > 0) {
            HelpdeskTicket existing = helpdeskTicketRepository.findById(ticket.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("HelpdeskTicket record not found with ID: " + ticket.getId());
            }

            if (ticket.getCategory() != null) existing.setCategory(ticket.getCategory());
            if (ticket.getPriority() != null) existing.setPriority(ticket.getPriority());
            if (ticket.getSubject() != null) existing.setSubject(ticket.getSubject());
            if (ticket.getDescription() != null) existing.setDescription(ticket.getDescription());
            if (ticket.getResolutionComments() != null) existing.setResolutionComments(ticket.getResolutionComments());
            if (ticket.getStatus() != null) existing.setStatus(ticket.getStatus());
            if (ticket.getIsActive() != null) existing.setIsActive(ticket.getIsActive());
            if (ticket.getResolvedAt() != null) existing.setResolvedAt(ticket.getResolvedAt());

            HelpdeskTicket updated = helpdeskTicketRepository.save(existing);
            return ApiResponse.success("HelpdeskTicket updated successfully", updated);
        }

        if (ticket.getSubject() == null || ticket.getSubject().trim().isEmpty()) {
            return ApiResponse.error("Subject is required");
        }
        if (ticket.getDescription() == null || ticket.getDescription().trim().isEmpty()) {
            return ApiResponse.error("Description is required");
        }

        if (ticket.getTicketNumber() == null || ticket.getTicketNumber().trim().isEmpty()) {
            ticket.setTicketNumber("TICK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (ticket.getCategory() == null) ticket.setCategory("HR");
        if (ticket.getPriority() == null) ticket.setPriority("MEDIUM");
        if (ticket.getStatus() == null) ticket.setStatus("OPEN");
        if (ticket.getIsActive() == null) ticket.setIsActive(true);

        HelpdeskTicket saved = helpdeskTicketRepository.save(ticket);
        return ApiResponse.success("HelpdeskTicket created successfully", saved);
    }

    @Transactional
    public ApiResponse<HelpdeskTicket> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid HelpdeskTicket ID is required");
        }
        HelpdeskTicket ticket = helpdeskTicketRepository.findById(id).orElse(null);
        if (ticket == null) {
            return ApiResponse.error("HelpdeskTicket record not found with ID: " + id);
        }

        boolean newStatus = !Boolean.TRUE.equals(ticket.getIsActive());
        ticket.setIsActive(newStatus);
        ticket.setStatus(newStatus ? "OPEN" : "INACTIVE");

        HelpdeskTicket updated = helpdeskTicketRepository.save(ticket);
        return ApiResponse.success("HelpdeskTicket active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<HelpdeskTicket>> getHelpdeskTicketsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long unitId = sessionData.getUnitId();
        List<HelpdeskTicket> list;
        if (unitId != null && unitId > 0) {
            list = helpdeskTicketRepository.findByUnitId(unitId);
        } else {
            list = helpdeskTicketRepository.findAll();
        }
        return ApiResponse.success("HelpdeskTickets retrieved successfully", list);
    }
}
