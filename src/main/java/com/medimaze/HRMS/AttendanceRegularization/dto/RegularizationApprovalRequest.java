package com.medimaze.HRMS.AttendanceRegularization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegularizationApprovalRequest {

    private Long id;
    private String status;
    private String remarks;
    private Long approverId;
}
