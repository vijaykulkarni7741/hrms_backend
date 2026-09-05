package com.webicSoftIndia.HRMS.Shift;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    Optional<Shift> findByShiftCode(String shiftCode);
    List<Shift> findByUnitId(Long unitId);
}

