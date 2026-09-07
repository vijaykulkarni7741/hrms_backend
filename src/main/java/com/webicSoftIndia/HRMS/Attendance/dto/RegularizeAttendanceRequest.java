package com.webicSoftIndia.HRMS.Attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegularizeAttendanceRequest {
    private Long attendanceId;
    private String attendanceDate;
    private String inTime;
    private String outTime;
    private String reason;
}
