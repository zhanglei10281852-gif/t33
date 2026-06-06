package com.prison.controller.psych;

import com.prison.dto.psych.CrisisInterventionDTO;
import com.prison.entity.psych.CrisisIntervention;
import com.prison.service.psych.CrisisInterventionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/psych/crisis-interventions")
@RequiredArgsConstructor
public class CrisisInterventionController {

    private final CrisisInterventionService interventionService;

    @GetMapping
    public ResponseEntity<?> listInterventions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String counselor,
            @RequestParam(required = false) String urgency) {
        Page<CrisisIntervention> result = interventionService.listInterventions(page, size, status, counselor, urgency);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIntervention(@PathVariable Long id) {
        return interventionService.getInterventionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/inmate/{inmateId}")
    public ResponseEntity<?> getByInmate(@PathVariable Long inmateId) {
        List<CrisisIntervention> interventions = interventionService.getInterventionsByInmate(inmateId);
        return ResponseEntity.ok(interventions);
    }

    @PostMapping
    public ResponseEntity<?> createIntervention(@Valid @RequestBody CrisisInterventionDTO dto) {
        CrisisIntervention intervention = interventionService.createIntervention(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(intervention);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateIntervention(@PathVariable Long id, @RequestBody CrisisInterventionDTO dto) {
        CrisisIntervention intervention = interventionService.updateIntervention(id, dto);
        if (intervention == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(intervention);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        CrisisIntervention intervention = interventionService.updateStatus(id, status);
        if (intervention == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(intervention);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIntervention(@PathVariable Long id) {
        interventionService.deleteIntervention(id);
        return ResponseEntity.noContent().build();
    }
}
