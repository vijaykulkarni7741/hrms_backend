package com.medimaze.HRMS.Organization;

import com.medimaze.HRMS.Employee.dto.EmployeeDto;
import com.medimaze.HRMS.common.ApiResponse;
import com.medimaze.HRMS.security.RedisSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final RedisSessionService redisSessionService;

    @Transactional
    public ApiResponse<Organization> saveOrUpdateOrganization(Organization organization) {
        if (organization == null) {
            return ApiResponse.error("Organization payload cannot be null");
        }

        // UPDATE MODE
        if (organization.getId() != null && organization.getId() > 0) {
            Organization existing = organizationRepository.findById(organization.getId()).orElse(null);
            if (existing == null) {
                return ApiResponse.error("Organization not found with ID: " + organization.getId());
            }

            if (organization.getOrganizationName() != null && !organization.getOrganizationName().trim().isEmpty()) {
                existing.setOrganizationName(organization.getOrganizationName().trim());
            }
            if (organization.getOrganizationShortName() != null) {
                existing.setOrganizationShortName(organization.getOrganizationShortName());
            }
            if (organization.getOrganizationType() != null) {
                existing.setOrganizationType(organization.getOrganizationType());
            }
            if (organization.getRegistrationNumber() != null) {
                existing.setRegistrationNumber(organization.getRegistrationNumber());
            }
            if (organization.getGstNumber() != null) {
                existing.setGstNumber(organization.getGstNumber());
            }
            if (organization.getPanNumber() != null) {
                existing.setPanNumber(organization.getPanNumber());
            }
            if (organization.getTanNumber() != null) {
                existing.setTanNumber(organization.getTanNumber());
            }
            if (organization.getCompanyLogo() != null) {
                existing.setCompanyLogo(organization.getCompanyLogo());
            }
            if (organization.getWebsite() != null) {
                existing.setWebsite(organization.getWebsite());
            }
            if (organization.getEmail() != null) {
                existing.setEmail(organization.getEmail());
            }
            if (organization.getMobileNumber() != null) {
                existing.setMobileNumber(organization.getMobileNumber());
            }
            if (organization.getAlternateMobile() != null) {
                existing.setAlternateMobile(organization.getAlternateMobile());
            }
            if (organization.getLandlineNumber() != null) {
                existing.setLandlineNumber(organization.getLandlineNumber());
            }
            if (organization.getAddressLine1() != null) {
                existing.setAddressLine1(organization.getAddressLine1());
            }
            if (organization.getAddressLine2() != null) {
                existing.setAddressLine2(organization.getAddressLine2());
            }
            if (organization.getCity() != null) {
                existing.setCity(organization.getCity());
            }
            if (organization.getPinCode() != null) {
                existing.setPinCode(organization.getPinCode());
            }
            if (organization.getTimeZone() != null) {
                existing.setTimeZone(organization.getTimeZone());
            }
            if (organization.getCurrency() != null) {
                existing.setCurrency(organization.getCurrency());
            }
            if (organization.getFinancialYearStart() != null) {
                existing.setFinancialYearStart(organization.getFinancialYearStart());
            }
            if (organization.getFinancialYearEnd() != null) {
                existing.setFinancialYearEnd(organization.getFinancialYearEnd());
            }
            if (organization.getWorkingDays() != null) {
                existing.setWorkingDays(organization.getWorkingDays());
            }
            if (organization.getRemarks() != null) {
                existing.setRemarks(organization.getRemarks());
            }
            if (organization.getStatus() != null) {
                existing.setStatus(organization.getStatus());
            }
            if (organization.getIsActive() != null) {
                existing.setIsActive(organization.getIsActive());
            }

            Organization updated = organizationRepository.save(existing);
            log.info("Updated Organization ID: {}", updated.getId());
            return ApiResponse.success("Organization updated successfully", updated);
        }

        // CREATE MODE
        if (organization.getOrganizationCode() == null || organization.getOrganizationCode().trim().isEmpty()) {
            return ApiResponse.error("Organization code is required");
        }
        if (organization.getOrganizationName() == null || organization.getOrganizationName().trim().isEmpty()) {
            return ApiResponse.error("Organization name is required");
        }

        if (organizationRepository.findByOrganizationCode(organization.getOrganizationCode().trim()).isPresent()) {
            return ApiResponse.error("Organization code already exists");
        }

        if (organization.getStatus() == null) {
            organization.setStatus("ACTIVE");
        }
        if (organization.getIsActive() == null) {
            organization.setIsActive(true);
        }

        Organization saved = organizationRepository.save(organization);
        log.info("Created Organization ID: {}", saved.getId());
        return ApiResponse.success("Organization created successfully", saved);
    }

    @Transactional
    public ApiResponse<Organization> toggleActiveStatus(Long id) {
        if (id == null || id <= 0) {
            return ApiResponse.error("Valid Organization ID is required");
        }
        Organization organization = organizationRepository.findById(id).orElse(null);
        if (organization == null) {
            return ApiResponse.error("Organization not found with ID: " + id);
        }

        boolean currentStatus = Boolean.TRUE.equals(organization.getIsActive());
        boolean newStatus = !currentStatus;
        organization.setIsActive(newStatus);
        organization.setStatus(newStatus ? "ACTIVE" : "INACTIVE");

        Organization updated = organizationRepository.save(organization);
        log.info("Toggled active status for Organization ID: {} to {}", id, newStatus);
        return ApiResponse.success("Organization active status toggled successfully to " + newStatus, updated);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<Organization>> getOrganizationsBySessionUnit(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error("Session ID header is required");
        }
        EmployeeDto sessionData = redisSessionService.getSession(sessionId.trim());
        if (sessionData == null) {
            return ApiResponse.error("Invalid or expired session in Redis");
        }

        Long orgId = sessionData.getOrganizationId();
        List<Organization> list;
        if (orgId != null && orgId > 0) {
            list = organizationRepository.findById(orgId)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        } else {
            list = organizationRepository.findAll();
        }
        return ApiResponse.success("Organizations retrieved successfully", list);
    }
}

