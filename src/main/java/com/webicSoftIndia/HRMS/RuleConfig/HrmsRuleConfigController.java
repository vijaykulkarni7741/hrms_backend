package com.webicSoftIndia.HRMS.RuleConfig;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/rules", "/v1/rules", "/api/v1/admin/rules", "/v1/admin/rules", "/admin/rules"})
@RequiredArgsConstructor
public class HrmsRuleConfigController {

    private final HrmsRuleConfigService ruleConfigService;

    private String extractSessionId(String xSessionId, String sessionIdHeader, String sessionIdParam) {
        if (xSessionId != null && !xSessionId.trim().isEmpty()) {
            return xSessionId.trim();
        } else if (sessionIdHeader != null && !sessionIdHeader.trim().isEmpty()) {
            return sessionIdHeader.trim();
        } else if (sessionIdParam != null && !sessionIdParam.trim().isEmpty()) {
            return sessionIdParam.trim();
        }
        return null;
    }

    @GetMapping({"/get", "/current"})
    public ResponseEntity<ApiResponse<HrmsRuleConfig>> getRuleConfig(
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {

        String sessionId = extractSessionId(xSessionId, sessionIdHeader, sessionIdParam);
        ApiResponse<HrmsRuleConfig> response = ruleConfigService.getRuleConfigBySession(sessionId);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/save", "/update"})
    public ResponseEntity<ApiResponse<HrmsRuleConfig>> saveRuleConfig(
            @RequestBody HrmsRuleConfig request,
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {

        String sessionId = extractSessionId(xSessionId, sessionIdHeader, sessionIdParam);
        ApiResponse<HrmsRuleConfig> response = ruleConfigService.saveOrUpdateRuleConfig(request, sessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
