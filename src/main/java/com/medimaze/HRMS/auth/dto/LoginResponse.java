package com.medimaze.HRMS.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String sessionId;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long expiresIn;
}
