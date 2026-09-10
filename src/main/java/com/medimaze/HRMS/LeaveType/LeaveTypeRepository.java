package com.medimaze.HRMS.LeaveType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {
    List<LeaveType> findByUnitId(Long unitId);
    Optional<LeaveType> findFirstByTypeCodeIgnoreCase(String typeCode);
    Optional<LeaveType> findFirstByTypeNameIgnoreCase(String typeName);
    Optional<LeaveType> findFirstByUnitIdAndIsActiveTrue(Long unitId);
}
