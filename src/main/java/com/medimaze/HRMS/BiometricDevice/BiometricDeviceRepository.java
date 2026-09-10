package com.medimaze.HRMS.BiometricDevice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BiometricDeviceRepository extends JpaRepository<BiometricDevice, Long> {
    Optional<BiometricDevice> findByDeviceCode(String deviceCode);
    List<BiometricDevice> findByUnitId(Long unitId);
}
