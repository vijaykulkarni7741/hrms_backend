package com.medimaze.HRMS.Holiday;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    Optional<Holiday> findByHolidayCode(String holidayCode);
    List<Holiday> findByUnitId(Long unitId);
}

