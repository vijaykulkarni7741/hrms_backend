package com.webicSoftIndia.HRMS.Attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkAttendanceRequest {
    private Long employeeId;
    private String selfieImage; // Base64 image URL / live selfie photo
    private String location;    // Work Location or GPS coordinates
    private String remarks;
}
