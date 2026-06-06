package com.prison.controller.psych;

import com.prison.dto.psych.PsychScaleDTO;
import com.prison.entity.psych.*;
import com.prison.service.psych.PsychScaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/psych/scales")
@RequiredArgsConstructor
public class PsychScaleController {

    private final PsychScaleService scaleService;

    @GetMapping
    public ResponseEntity<?> listScales() {
        List<PsychScale> scales = scaleService.listAllScales();
        return ResponseEntity.ok(scales);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getScale(@PathVariable Long id) {
        return scaleService.getScaleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<?> getScaleByCode(@PathVariable String code) {
        return scaleService.getScaleByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<?> getQuestions(@PathVariable Long id) {
        List<PsychScaleQuestion> questions = scaleService.getQuestionsByScaleId(id);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/questions/{questionId}/options")
    public ResponseEntity<?> getOptions(@PathVariable Long questionId) {
        List<PsychScaleOption> options = scaleService.getOptionsByQuestionId(questionId);
        return ResponseEntity.ok(options);
    }

    @GetMapping("/{id}/factors")
    public ResponseEntity<?> getFactors(@PathVariable Long id) {
        List<PsychScaleFactor> factors = scaleService.getFactorsByScaleId(id);
        return ResponseEntity.ok(factors);
    }

    @PostMapping
    public ResponseEntity<?> createScale(@Valid @RequestBody PsychScaleDTO dto) {
        PsychScale scale = scaleService.createScale(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(scale);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateScale(@PathVariable Long id, @Valid @RequestBody PsychScaleDTO dto) {
        PsychScale scale = scaleService.updateScale(id, dto);
        if (scale == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(scale);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteScale(@PathVariable Long id) {
        scaleService.deleteScale(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/questions")
    public ResponseEntity<?> addQuestion(@PathVariable Long id, @RequestBody PsychScaleQuestion question) {
        PsychScaleQuestion saved = scaleService.addQuestion(id, question);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/questions/{questionId}/options")
    public ResponseEntity<?> addOption(@PathVariable Long questionId, @RequestBody PsychScaleOption option) {
        PsychScaleOption saved = scaleService.addOption(questionId, option);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/{id}/factors")
    public ResponseEntity<?> addFactor(@PathVariable Long id, @RequestBody PsychScaleFactor factor) {
        PsychScaleFactor saved = scaleService.addFactor(id, factor);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<?> getScaleDetail(@PathVariable Long id) {
        return scaleService.getScaleById(id).map(scale -> {
            Map<String, Object> detail = new HashMap<>();
            detail.put("scale", scale);
            detail.put("questions", scaleService.getQuestionsByScaleId(id));
            detail.put("factors", scaleService.getFactorsByScaleId(id));
            return ResponseEntity.ok(detail);
        }).orElse(ResponseEntity.notFound().build());
    }
}
