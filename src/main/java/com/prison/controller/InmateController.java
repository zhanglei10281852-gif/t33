package com.prison.controller;

import com.prison.dto.InmateDTO;
import com.prison.entity.Inmate;
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
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/inmates")
@RequiredArgsConstructor
public class InmateController {

    private final InmateRepository inmateRepository;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody InmateDTO dto) {
        if (inmateRepository.findByInmateNo(dto.getInmateNo()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "编号已存在"));
        }
        Inmate inmate = new Inmate();
        inmate.setInmateNo(dto.getInmateNo());
        inmate.setName(dto.getName());
        inmate.setIdCard(dto.getIdCard());
        inmate.setGender(dto.getGender());
        inmate.setBirthDate(LocalDate.parse(dto.getBirthDate()));
        inmate.setCrime(dto.getCrime());
        inmate.setSentenceMonths(dto.getSentenceMonths());
        inmate.setAdmissionDate(LocalDate.parse(dto.getAdmissionDate()));
        inmate.setWard(dto.getWard());
        inmate.setCellNo(dto.getCellNo());
        Inmate saved = inmateRepository.save(inmate);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Inmate> result;
        if (keyword != null && !keyword.isBlank()) {
            result = inmateRepository.search(keyword, pageRequest);
        } else if (ward != null && !ward.isBlank()) {
            result = inmateRepository.findByWard(ward, pageRequest);
        } else if (status != null && !status.isBlank()) {
            result = inmateRepository.findByStatus(status, pageRequest);
        } else {
            result = inmateRepository.findAll(pageRequest);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return inmateRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return inmateRepository.findById(id).map(inmate -> {
            if (updates.containsKey("ward")) inmate.setWard((String) updates.get("ward"));
            if (updates.containsKey("cellNo")) inmate.setCellNo((String) updates.get("cellNo"));
            if (updates.containsKey("status")) inmate.setStatus((String) updates.get("status"));
            return ResponseEntity.ok(inmateRepository.save(inmate));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", inmateRepository.count());
        stats.put("inCustody", inmateRepository.countByStatus("在押"));
        stats.put("released", inmateRepository.countByStatus("已释放"));
        stats.put("transferred", inmateRepository.countByStatus("已转监"));
        return ResponseEntity.ok(stats);
    }
}
