package com.prison.service.psych;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prison.entity.Inmate;
import com.prison.entity.psych.AssessmentResult;
import com.prison.entity.psych.CrisisIntervention;
import com.prison.repository.InmateRepository;
import com.prison.repository.psych.AssessmentResultRepository;
import com.prison.repository.psych.AssessmentTaskRepository;
import com.prison.repository.psych.CrisisInterventionRepository;
import com.prison.repository.psych.PsychScaleFactorRepository;
import com.prison.repository.psych.PsychScaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PsychStatsService {

    private final AssessmentResultRepository resultRepository;
    private final AssessmentTaskRepository taskRepository;
    private final CrisisInterventionRepository interventionRepository;
    private final InmateRepository inmateRepository;
    private final PsychScaleFactorRepository factorRepository;
    private final PsychScaleRepository scaleRepository;
    private final ObjectMapper objectMapper;

    public Map<String, Object> getRiskLevelDistribution() {
        Map<String, Object> result = new HashMap<>();
        
        List<Inmate> allInmates = inmateRepository.findByStatus("在押", org.springframework.data.domain.Pageable.unpaged()).getContent();
        long total = allInmates.size();
        
        Map<String, Long> latestRiskCount = new HashMap<>();
        latestRiskCount.put("高风险", 0L);
        latestRiskCount.put("中风险", 0L);
        latestRiskCount.put("低风险", 0L);
        latestRiskCount.put("未评估", 0L);

        Set<Long> assessedInmates = new HashSet<>();
        List<AssessmentResult> allResults = resultRepository.findAll();
        
        Map<Long, Map<String, AssessmentResult>> latestByInmateAndScale = new HashMap<>();
        for (AssessmentResult r : allResults) {
            if (!latestByInmateAndScale.containsKey(r.getInmateId())) {
                latestByInmateAndScale.put(r.getInmateId(), new HashMap<>());
            }
            Map<String, AssessmentResult> scaleMap = latestByInmateAndScale.get(r.getInmateId());
            if (r.getScaleCode() != null) {
                if (!scaleMap.containsKey(r.getScaleCode()) || 
                    r.getCreatedAt().isAfter(scaleMap.get(r.getScaleCode()).getCreatedAt())) {
                    scaleMap.put(r.getScaleCode(), r);
                }
            }
        }

        for (Inmate inmate : allInmates) {
            Map<String, AssessmentResult> scaleMap = latestByInmateAndScale.get(inmate.getId());
            if (scaleMap == null || scaleMap.isEmpty()) {
                latestRiskCount.merge("未评估", 1L, Long::sum);
            } else {
                String highest = "低风险";
                for (AssessmentResult r : scaleMap.values()) {
                    String level = r.getRiskLevel();
                    if ("高风险".equals(level)) {
                        highest = "高风险";
                        break;
                    } else if ("中风险".equals(level)) {
                        highest = "中风险";
                    }
                }
                latestRiskCount.merge(highest, 1L, Long::sum);
            }
        }

        result.put("total", total);
        result.put("distribution", latestRiskCount);
        return result;
    }

    public Map<String, Object> getWardCompletionRate() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Map<String, Long>> wardStats = new HashMap<>();

        List<Inmate> inmates = inmateRepository.findByStatus("在押", org.springframework.data.domain.Pageable.unpaged()).getContent();
        for (Inmate inmate : inmates) {
            String ward = inmate.getWard();
            if (!wardStats.containsKey(ward)) {
                Map<String, Long> stats = new HashMap<>();
                stats.put("total", 0L);
                stats.put("completed", 0L);
                stats.put("pending", 0L);
                wardStats.put(ward, stats);
            }
            wardStats.get(ward).merge("total", 1L, Long::sum);
        }

        List<com.prison.entity.psych.AssessmentTask> allTasks = taskRepository.findAll();
        Set<Long> completedInmates = new HashSet<>();
        Set<Long> pendingInmates = new HashSet<>();
        
        for (com.prison.entity.psych.AssessmentTask task : allTasks) {
            if ("已完成".equals(task.getStatus())) {
                completedInmates.add(task.getInmateId());
            } else if ("待完成".equals(task.getStatus())) {
                pendingInmates.add(task.getInmateId());
            }
        }

        for (Inmate inmate : inmates) {
            String ward = inmate.getWard();
            if (completedInmates.contains(inmate.getId())) {
                wardStats.get(ward).merge("completed", 1L, Long::sum);
            }
            if (pendingInmates.contains(inmate.getId())) {
                wardStats.get(ward).merge("pending", 1L, Long::sum);
            }
        }

        Map<String, Double> completionRates = new HashMap<>();
        for (Map.Entry<String, Map<String, Long>> entry : wardStats.entrySet()) {
            long total = entry.getValue().getOrDefault("total", 0L);
            long completed = entry.getValue().getOrDefault("completed", 0L);
            double rate = total > 0 ? (double) completed / total * 100 : 0;
            completionRates.put(entry.getKey(), Math.round(rate * 100.0) / 100.0);
        }

        result.put("wardStats", wardStats);
        result.put("completionRates", completionRates);
        return result;
    }

    public Map<String, Object> getInterventionEffectiveness() {
        Map<String, Object> result = new HashMap<>();
        long totalCompleted = interventionRepository.countCompletedInterventions();
        long effective = interventionRepository.countEffectiveInterventions();
        double rate = totalCompleted > 0 ? (double) effective / totalCompleted * 100 : 0;

        result.put("totalCompleted", totalCompleted);
        result.put("effective", effective);
        result.put("effectivenessRate", Math.round(rate * 100.0) / 100.0);
        return result;
    }

    public List<Map<String, Object>> getMonthlyHighRiskTrend(int months) {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1);
            
            LocalDateTime start = monthStart.atStartOfDay();
            LocalDateTime end = monthEnd.atStartOfDay();
            
            long count = resultRepository.countByRiskLevelAndCreatedAtBetween("高风险", start, end);
            
            Map<String, Object> item = new HashMap<>();
            item.put("month", monthStart.toString().substring(0, 7));
            item.put("highRiskCount", count);
            result.add(item);
        }
        return result;
    }

    public Map<String, Object> getFactorAbnormalityDistribution() {
        Map<String, Object> result = new HashMap<>();
        Map<String, Map<String, Long>> factorStats = new HashMap<>();

        Optional<com.prison.entity.psych.PsychScale> scl90Opt = scaleRepository.findTopByCodeOrderByVersionDesc("SCL90");
        if (scl90Opt.isEmpty()) {
            result.put("factors", factorStats);
            return result;
        }

        List<com.prison.entity.psych.PsychScaleFactor> factors = factorRepository.findByScaleId(scl90Opt.get().getId());
        for (com.prison.entity.psych.PsychScaleFactor f : factors) {
            Map<String, Long> stats = new HashMap<>();
            stats.put("highAbnormal", 0L);
            stats.put("mediumAbnormal", 0L);
            stats.put("normal", 0L);
            factorStats.put(f.getFactorName(), stats);
        }

        List<AssessmentResult> scl90Results = resultRepository.findAll().stream()
                .filter(r -> "SCL90".equals(r.getScaleCode()))
                .collect(Collectors.toList());

        Map<Long, AssessmentResult> latestByInmate = new HashMap<>();
        for (AssessmentResult r : scl90Results) {
            if (!latestByInmate.containsKey(r.getInmateId()) || 
                r.getCreatedAt().isAfter(latestByInmate.get(r.getInmateId()).getCreatedAt())) {
                latestByInmate.put(r.getInmateId(), r);
            }
        }

        for (AssessmentResult r : latestByInmate.values()) {
            try {
                Map<String, Double> factorScores = objectMapper.readValue(
                        r.getFactorScores(), new TypeReference<Map<String, Double>>() {});
                for (Map.Entry<String, Double> entry : factorScores.entrySet()) {
                    String factorName = entry.getKey();
                    double score = entry.getValue();
                    if (factorStats.containsKey(factorName)) {
                        if (score >= 3) {
                            factorStats.get(factorName).merge("highAbnormal", 1L, Long::sum);
                        } else if (score >= 2) {
                            factorStats.get(factorName).merge("mediumAbnormal", 1L, Long::sum);
                        } else {
                            factorStats.get(factorName).merge("normal", 1L, Long::sum);
                        }
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }

        result.put("totalAssessed", latestByInmate.size());
        result.put("factorDistribution", factorStats);
        return result;
    }

    public Map<String, Object> getPsychologicalFile(Long inmateId) {
        Map<String, Object> file = new HashMap<>();

        Optional<Inmate> inmateOpt = inmateRepository.findById(inmateId);
        if (inmateOpt.isEmpty()) {
            return null;
        }
        file.put("inmate", inmateOpt.get());

        List<AssessmentResult> results = resultRepository.findByInmateIdOrderByCreatedAtDesc(inmateId);
        file.put("assessmentResults", results);

        List<CrisisIntervention> interventions = interventionRepository.findByInmateIdOrderByCreatedAtDesc(inmateId);
        file.put("interventionRecords", interventions);

        List<Map<String, Object>> riskTrend = new ArrayList<>();
        for (AssessmentResult r : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", r.getCreatedAt().toLocalDate().toString());
            item.put("scale", r.getScaleCode());
            item.put("riskLevel", r.getRiskLevel());
            item.put("score", r.getTotalScore());
            riskTrend.add(item);
        }
        file.put("riskTrend", riskTrend);

        String currentRisk = "未评估";
        if (!results.isEmpty()) {
            Map<String, AssessmentResult> latestByScale = new HashMap<>();
            for (AssessmentResult r : results) {
                if (r.getScaleCode() != null && !latestByScale.containsKey(r.getScaleCode())) {
                    latestByScale.put(r.getScaleCode(), r);
                }
            }
            String highest = "低风险";
            for (AssessmentResult r : latestByScale.values()) {
                String level = r.getRiskLevel();
                if ("高风险".equals(level)) {
                    highest = "高风险";
                    break;
                } else if ("中风险".equals(level)) {
                    highest = "中风险";
                }
            }
            currentRisk = highest;
        }
        file.put("currentRiskLevel", currentRisk);
        file.put("assessmentCount", results.size());
        file.put("interventionCount", interventions.size());

        return file;
    }
}
