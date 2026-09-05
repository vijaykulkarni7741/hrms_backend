package com.webicSoftIndia.HRMS.EscalationRule;

import com.webicSoftIndia.HRMS.common.ApiResponse;
import com.webicSoftIndia.HRMS.common.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/escalation-rules")
@RequiredArgsConstructor
public class EscalationRuleController {

    private final EscalationRuleService escalationRuleService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<EscalationRule>> createOrUpdateEscalationRule(@RequestBody EscalationRule rule) {
        ApiResponse<EscalationRule> response = escalationRuleService.saveOrUpdateEscalationRule(rule);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/toggle-active")
    public ResponseEntity<ApiResponse<EscalationRule>> toggleActiveStatus(@RequestBody IdRequest request) {
        Long id = request != null ? request.getId() : null;
        ApiResponse<EscalationRule> response = escalationRuleService.toggleActiveStatus(id);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<EscalationRule>>> listEscalationRules(
            @RequestHeader(value = "X-Session-ID", required = false) String xSessionId,
            @RequestHeader(value = "Session-ID", required = false) String sessionIdHeader,
            @RequestHeader(value = "sessionId", required = false) String sessionIdParam) {

        String targetSessionId = null;
        if (xSessionId != null && !xSessionId.trim().isEmpty()) {
            targetSessionId = xSessionId.trim();
        } else if (sessionIdHeader != null && !sessionIdHeader.trim().isEmpty()) {
            targetSessionId = sessionIdHeader.trim();
        } else if (sessionIdParam != null && !sessionIdParam.trim().isEmpty()) {
            targetSessionId = sessionIdParam.trim();
        }

        ApiResponse<List<EscalationRule>> response = escalationRuleService.getEscalationRulesBySessionUnit(targetSessionId);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
