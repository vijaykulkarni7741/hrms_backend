package com.webicSoftIndia.HRMS.FnFSettlement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FnFSettlementRepository extends JpaRepository<FnFSettlement, Long> {
    List<FnFSettlement> findByUnitId(Long unitId);
}
