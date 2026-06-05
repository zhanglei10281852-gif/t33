package com.prison.controller;

import com.prison.dto.VisitorDTO;
import com.prison.entity.Visitor;
import com.prison.repository.InmateRepository;
import com.prison.repository.VisitorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/visitors")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorRepository visitorRepository;
    private final InmateRepository inmateRepository;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody VisitorDTO dto) {
        if (!inmateRepository.existsById(dto.getInmateId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "服刑人员不存在"));
        }
        Visitor visitor = new Visitor();
        visitor.setInmateId(dto.getInmateId());
        visitor.setVisitorName(dto.getVisitorName());
        visitor.setVisitorIdCard(dto.getVisitorIdCard());
        visitor.setRelationship(dto.getRelationship());
        visitor.setPhone(dto.getPhone());
        visitor.setVisitDate(LocalDate.parse(dto.getVisitDate()));
        visitor.setTimeSlot(dto.getTimeSlot());
        visitor.setRemark(dto.getRemark());
        Visitor saved = visitorRepository.save(visitor);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) Long inmateId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (inmateId != null) {
            return ResponseEntity.ok(visitorRepository.findByInmateId(inmateId, pageRequest));
        }
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(visitorRepository.findByStatus(status, pageRequest));
        }
        return ResponseEntity.ok(visitorRepository.findAll(pageRequest));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return visitorRepository.findById(id).map(visitor -> {
            String action = body.getOrDefault("action", "");
            if ("approve".equals(action)) {
                visitor.setStatus("已批准");
            } else if ("reject".equals(action)) {
                visitor.setStatus("已拒绝");
                visitor.setRemark(body.getOrDefault("reason", ""));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "无效操作"));
            }
            visitorRepository.save(visitor);
            return ResponseEntity.ok(Map.of("message", "操作成功", "status", visitor.getStatus()));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/today")
    public ResponseEntity<?> today() {
        return ResponseEntity.ok(Map.of(
                "total", visitorRepository.countByVisitDate(LocalDate.now()),
                "approved", visitorRepository.findByVisitDateAndStatus(LocalDate.now(), "已批准")
        ));
    }
}
