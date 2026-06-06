package com.prison.controller.psych;

import com.prison.dto.psych.AssessmentSubmitDTO;
import com.prison.entity.psych.AssessmentAnswer;
import com.prison.entity.psych.AssessmentResponse;
import com.prison.entity.psych.AssessmentResult;
import com.prison.service.psych.AssessmentScoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/psych/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentScoringService scoringService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitAssessment(@Valid @RequestBody AssessmentSubmitDTO dto) {
        AssessmentResult result = scoringService.submitAssessment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/results/{id}")
    public ResponseEntity<?> getResult(@PathVariable Long id) {
        return scoringService.getResultById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/results/task/{taskId}")
    public ResponseEntity<?> getResultByTask(@PathVariable Long taskId) {
        return scoringService.getResultByTaskId(taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/results/inmate/{inmateId}")
    public ResponseEntity<?> getResultsByInmate(@PathVariable Long inmateId) {
        List<AssessmentResult> results = scoringService.getResultsByInmateId(inmateId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/responses/{responseId}/answers")
    public ResponseEntity<?> getAnswers(@PathVariable Long responseId) {
        List<AssessmentAnswer> answers = scoringService.getAnswersByResponseId(responseId);
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/responses/task/{taskId}")
    public ResponseEntity<?> getResponseByTask(@PathVariable Long taskId) {
        return scoringService.getResponseByTaskId(taskId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/risk/inmate/{inmateId}")
    public ResponseEntity<?> getComprehensiveRisk(@PathVariable Long inmateId) {
        String riskLevel = scoringService.determineComprehensiveRiskLevel(inmateId);
        Map<String, String> result = new HashMap<>();
        result.put("riskLevel", riskLevel);
        result.put("inmateId", String.valueOf(inmateId));
        return ResponseEntity.ok(result);
    }
}
