package com.prison.controller.psych;

import com.prison.service.psych.PsychStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/psych/files")
@RequiredArgsConstructor
public class PsychFileController {

    private final PsychStatsService statsService;

    @GetMapping("/{inmateId}")
    public ResponseEntity<?> getPsychologicalFile(@PathVariable Long inmateId) {
        java.util.Map<String, Object> file = statsService.getPsychologicalFile(inmateId);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(file);
    }
}
