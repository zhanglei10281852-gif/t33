package com.prison.controller.psych;

import com.prison.service.psych.PsychStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/psych/stats")
@RequiredArgsConstructor
public class PsychStatsController {

    private final PsychStatsService statsService;

    @GetMapping("/risk-distribution")
    public ResponseEntity<?> getRiskDistribution() {
        Map<String, Object> result = statsService.getRiskLevelDistribution();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ward-completion")
    public ResponseEntity<?> getWardCompletionRate() {
        Map<String, Object> result = statsService.getWardCompletionRate();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/intervention-effectiveness")
    public ResponseEntity<?> getInterventionEffectiveness() {
        Map<String, Object> result = statsService.getInterventionEffectiveness();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/monthly-high-risk")
    public ResponseEntity<?> getMonthlyHighRiskTrend(
            @RequestParam(defaultValue = "6") int months) {
        List<Map<String, Object>> result = statsService.getMonthlyHighRiskTrend(months);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/factor-abnormality")
    public ResponseEntity<?> getFactorAbnormalityDistribution() {
        Map<String, Object> result = statsService.getFactorAbnormalityDistribution();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getOverviewStats() {
        Map<String, Object> overview = new java.util.HashMap<>();
        overview.put("riskDistribution", statsService.getRiskLevelDistribution());
        overview.put("interventionEffectiveness", statsService.getInterventionEffectiveness());
        overview.put("factorAbnormality", statsService.getFactorAbnormalityDistribution());
        return ResponseEntity.ok(overview);
    }
}
