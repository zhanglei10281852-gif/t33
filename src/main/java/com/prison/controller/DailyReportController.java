package com.prison.controller;

import com.prison.dto.DailyReportDTO;
import com.prison.entity.DailyReport;
import com.prison.repository.DailyReportRepository;
import com.prison.repository.InmateRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/daily-reports")
@RequiredArgsConstructor
public class DailyReportController {

    private final DailyReportRepository dailyReportRepository;
    private final InmateRepository inmateRepository;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody DailyReportDTO dto) {
        if (!inmateRepository.existsById(dto.getInmateId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "服刑人员不存在"));
        }
        LocalDate date = LocalDate.parse(dto.getReportDate());
        if (dailyReportRepository.existsByInmateIdAndReportDate(dto.getInmateId(), date)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "该日已有记录"));
        }
        DailyReport report = new DailyReport();
        report.setInmateId(dto.getInmateId());
        report.setReportDate(date);
        report.setHealthStatus(dto.getHealthStatus());
        report.setBehaviorNote(dto.getBehaviorNote());
        report.setMoodLevel(dto.getMoodLevel());
        report.setReporter(dto.getReporter());
        DailyReport saved = dailyReportRepository.save(report);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) Long inmateId,
            @RequestParam(required = false) String date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("reportDate").descending());
        if (inmateId != null) {
            return ResponseEntity.ok(dailyReportRepository.findByInmateId(inmateId, pageRequest));
        }
        if (date != null) {
            List<DailyReport> reports = dailyReportRepository.findByReportDate(LocalDate.parse(date));
            return ResponseEntity.ok(reports);
        }
        return ResponseEntity.ok(dailyReportRepository.findAll(pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return dailyReportRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
