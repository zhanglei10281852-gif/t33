package com.prison.controller.psych;

import com.prison.dto.psych.AssessmentTaskDTO;
import com.prison.entity.psych.AssessmentTask;
import com.prison.service.psych.AssessmentTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/psych/tasks")
@RequiredArgsConstructor
public class AssessmentTaskController {

    private final AssessmentTaskService taskService;

    @GetMapping
    public ResponseEntity<?> listTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long inmateId,
            @RequestParam(required = false) String taskType) {
        Page<AssessmentTask> result = taskService.listTasks(page, size, status, inmateId, taskType);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTask(@PathVariable Long id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody AssessmentTaskDTO dto) {
        AssessmentTask task = taskService.createTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        AssessmentTask task = taskService.updateTaskStatus(id, status);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pending/{inmateId}")
    public ResponseEntity<?> getPendingTasks(@PathVariable Long inmateId) {
        List<AssessmentTask> tasks = taskService.getPendingTasks(inmateId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AssessmentTask> result = taskService.getOverdueTasks(page, size);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/generate-intake/{inmateId}")
    public ResponseEntity<?> generateIntakeAssessment(@PathVariable Long inmateId) {
        taskService.generateIntakeAssessment(inmateId);
        Map<String, String> result = new HashMap<>();
        result.put("message", "入监评估任务已生成");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/generate-quarterly")
    public ResponseEntity<?> generateQuarterlyAssessments() {
        taskService.generateQuarterlyAssessments();
        Map<String, String> result = new HashMap<>();
        result.put("message", "季度定期评估任务已生成");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getTaskStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", taskService.listTasks(0, 1, null, null, null).getTotalElements());
        return ResponseEntity.ok(stats);
    }
}
