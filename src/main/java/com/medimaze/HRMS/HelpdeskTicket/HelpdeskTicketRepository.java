package com.medimaze.HRMS.HelpdeskTicket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelpdeskTicketRepository extends JpaRepository<HelpdeskTicket, Long> {
    List<HelpdeskTicket> findByUnitId(Long unitId);
}
