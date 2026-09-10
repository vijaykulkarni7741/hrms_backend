package com.medimaze.HRMS.Calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, Long> {
    Optional<Calendar> findByCalendarCode(String calendarCode);
    List<Calendar> findByUnitId(Long unitId);
}

