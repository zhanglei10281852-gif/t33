package com.prison.service.psych;

import com.prison.dto.psych.AssessmentSubmitDTO;
import com.prison.entity.psych.*;
import com.prison.repository.psych.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssessmentScoringService {

    private final AssessmentResponseRepository responseRepository;
    private final AssessmentAnswerRepository answerRepository;
    private final AssessmentResultRepository resultRepository;
    private final AssessmentTaskRepository taskRepository;
    private final PsychScaleQuestionRepository questionRepository;
    private final PsychScaleOptionRepository optionRepository;
    private final PsychScaleFactorRepository factorRepository;
    private final PsychScaleRepository scaleRepository;
    private final CrisisInterventionService crisisInterventionService;
    private final ObjectMapper objectMapper;

    @Transactional
    public AssessmentResult submitAssessment(AssessmentSubmitDTO dto) {
        AssessmentResponse response = new AssessmentResponse();
        response.setTaskId(dto.getTaskId());
        response.setInmateId(dto.getInmateId());
        response.setScaleId(dto.getScaleId());
        response.setSubmitTime(LocalDateTime.now());
        response.setRemark(dto.getRemark());
        AssessmentResponse savedResponse = responseRepository.save(response);

        List<PsychScaleQuestion> questions = questionRepository.findByScaleIdOrderByQuestionNo(dto.getScaleId());
        Map<Long, PsychScaleQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(PsychScaleQuestion::getId, q -> q));

        for (AssessmentSubmitDTO.AnswerItemDTO item : dto.getAnswers()) {
            AssessmentAnswer answer = new AssessmentAnswer();
            answer.setResponseId(savedResponse.getId());
            answer.setQuestionId(item.getQuestionId());
            answer.setQuestionNo(item.getQuestionNo());
            answer.setSelectedOptionNo(item.getSelectedOptionNo());
            answer.setAnswerValue(item.getAnswerValue());

            PsychScaleQuestion q = questionMap.get(item.getQuestionId());
            if (q != null && item.getSelectedOptionNo() != null) {
                List<PsychScaleOption> options = optionRepository.findByQuestionIdOrderByOptionNo(q.getId());
                for (PsychScaleOption opt : options) {
                    if (opt.getOptionNo().equals(item.getSelectedOptionNo())) {
                        int score = opt.getScore();
                        if (Boolean.TRUE.equals(q.getReverseScoring())) {
                            int maxScore = options.stream().mapToInt(PsychScaleOption::getScore).max().orElse(5);
                            int minScore = options.stream().mapToInt(PsychScaleOption::getScore).min().orElse(1);
                            score = maxScore + minScore - score;
                        }
                        answer.setScore(score);
                        break;
                    }
                }
            }
            answerRepository.save(answer);
        }

        AssessmentResult result = calculateScore(savedResponse.getId(), dto.getScaleId(), dto.getInmateId(), dto.getTaskId());

        taskRepository.findById(dto.getTaskId()).ifPresent(task -> {
            task.setStatus("已完成");
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);
        });

        if ("高风险".equals(result.getRiskLevel()) || "中风险".equals(result.getRiskLevel())) {
            crisisInterventionService.autoGenerateIntervention(
                    dto.getInmateId(), dto.getTaskId(), result.getId(), result.getRiskLevel());
        }

        return result;
    }

    @Transactional
    public AssessmentResult calculateScore(Long responseId, Long scaleId, Long inmateId, Long taskId) {
        List<AssessmentAnswer> answers = answerRepository.findByResponseIdOrderByQuestionNo(responseId);
        Map<Integer, Integer> answerScores = new HashMap<>();
        for (AssessmentAnswer a : answers) {
            if (a.getScore() != null) {
                answerScores.put(a.getQuestionNo(), a.getScore());
            }
        }

        PsychScale scale = scaleRepository.findById(scaleId).orElse(null);
        if (scale == null) {
            throw new RuntimeException("量表不存在");
        }

        AssessmentResult result = new AssessmentResult();
        result.setTaskId(taskId);
        result.setInmateId(inmateId);
        result.setScaleId(scaleId);
        result.setScaleCode(scale.getCode());

        String scaleType = scale.getScaleType();
        switch (scaleType) {
            case "SCL90":
                calculateSCL90(result, answerScores, scaleId);
                break;
            case "SDS":
                calculateSDS(result, answerScores);
                break;
            case "SAS":
                calculateSAS(result, answerScores);
                break;
            case "MMPI":
                calculateMMPI(result, answerScores);
                break;
            default:
                calculateDefault(result, answerScores);
        }

        result.setRiskLevel(determineRiskLevel(result, scaleType));

        return resultRepository.save(result);
    }

    private void calculateSCL90(AssessmentResult result, Map<Integer, Integer> answerScores, Long scaleId) {
        double totalScore = answerScores.values().stream().mapToInt(Integer::intValue).sum();
        result.setTotalScore(totalScore);
        result.setRawScore(totalScore);

        List<PsychScaleFactor> factors = factorRepository.findByScaleId(scaleId);
        Map<String, Double> factorScores = new LinkedHashMap<>();

        for (PsychScaleFactor factor : factors) {
            String[] qNos = factor.getQuestionNos().split(",");
            double sum = 0;
            int count = 0;
            for (String qNoStr : qNos) {
                int qNo = Integer.parseInt(qNoStr.trim());
                if (answerScores.containsKey(qNo)) {
                    sum += answerScores.get(qNo);
                    count++;
                }
            }
            if (count > 0) {
                factorScores.put(factor.getFactorName(), Math.round(sum / count * 100.0) / 100.0);
            }
        }

        try {
            result.setFactorScores(objectMapper.writeValueAsString(factorScores));
        } catch (Exception e) {
            result.setFactorScores("{}");
        }

        StringBuilder interpretation = new StringBuilder();
        interpretation.append("总分：").append(totalScore).append("。");
        interpretation.append("阳性项目数：").append(answerScores.values().stream().filter(s -> s > 1).count()).append("。");
        for (Map.Entry<String, Double> entry : factorScores.entrySet()) {
            if (entry.getValue() >= 3) {
                interpretation.append(entry.getKey()).append("因子分≥3，提示可能存在较严重的").append(entry.getKey()).append("症状；");
            } else if (entry.getValue() >= 2) {
                interpretation.append(entry.getKey()).append("因子分≥2，提示存在轻度").append(entry.getKey()).append("症状；");
            }
        }
        result.setInterpretation(interpretation.toString());
    }

    private void calculateSDS(AssessmentResult result, Map<Integer, Integer> answerScores) {
        double rawScore = answerScores.values().stream().mapToInt(Integer::intValue).sum();
        double standardScore = Math.floor(rawScore * 1.25);
        result.setRawScore(rawScore);
        result.setStandardScore(standardScore);
        result.setTotalScore(standardScore);

        StringBuilder interpretation = new StringBuilder();
        interpretation.append("粗分：").append(rawScore).append("，标准分：").append(standardScore).append("。");
        if (standardScore >= 70) {
            interpretation.append("重度抑郁");
        } else if (standardScore >= 63) {
            interpretation.append("中度抑郁");
        } else if (standardScore >= 53) {
            interpretation.append("轻度抑郁");
        } else {
            interpretation.append("正常");
        }
        result.setInterpretation(interpretation.toString());
    }

    private void calculateSAS(AssessmentResult result, Map<Integer, Integer> answerScores) {
        double rawScore = answerScores.values().stream().mapToInt(Integer::intValue).sum();
        double standardScore = Math.floor(rawScore * 1.25);
        result.setRawScore(rawScore);
        result.setStandardScore(standardScore);
        result.setTotalScore(standardScore);

        StringBuilder interpretation = new StringBuilder();
        interpretation.append("粗分：").append(rawScore).append("，标准分：").append(standardScore).append("。");
        if (standardScore >= 70) {
            interpretation.append("重度焦虑");
        } else if (standardScore >= 60) {
            interpretation.append("中度焦虑");
        } else if (standardScore >= 50) {
            interpretation.append("轻度焦虑");
        } else {
            interpretation.append("正常");
        }
        result.setInterpretation(interpretation.toString());
    }

    private void calculateMMPI(AssessmentResult result, Map<Integer, Integer> answerScores) {
        double rawScore = answerScores.values().stream().mapToInt(Integer::intValue).sum();
        result.setRawScore(rawScore);
        result.setTotalScore(rawScore);
        result.setInterpretation("MMPI简版得分：" + rawScore + "分");
    }

    private void calculateDefault(AssessmentResult result, Map<Integer, Integer> answerScores) {
        double totalScore = answerScores.values().stream().mapToInt(Integer::intValue).sum();
        result.setTotalScore(totalScore);
        result.setRawScore(totalScore);
        result.setInterpretation("总得分：" + totalScore);
    }

    private String determineRiskLevel(AssessmentResult result, String scaleType) {
        switch (scaleType) {
            case "SDS":
            case "SAS":
                if (result.getStandardScore() != null && result.getStandardScore() >= 70) {
                    return "高风险";
                } else if (result.getStandardScore() != null && result.getStandardScore() >= 53) {
                    return "中风险";
                } else {
                    return "低风险";
                }
            case "SCL90":
                try {
                    Map<String, Double> factorScores = objectMapper.readValue(
                            result.getFactorScores(), new TypeReference<Map<String, Double>>() {});
                    boolean hasHigh = factorScores.values().stream().anyMatch(v -> v >= 3);
                    boolean hasMedium = factorScores.values().stream().anyMatch(v -> v >= 2);
                    if (hasHigh) return "高风险";
                    if (hasMedium) return "中风险";
                    return "低风险";
                } catch (Exception e) {
                    return "低风险";
                }
            default:
                return "低风险";
        }
    }

    public Optional<AssessmentResult> getResultById(Long id) {
        return resultRepository.findById(id);
    }

    public Optional<AssessmentResult> getResultByTaskId(Long taskId) {
        return resultRepository.findByTaskId(taskId);
    }

    public List<AssessmentResult> getResultsByInmateId(Long inmateId) {
        return resultRepository.findByInmateIdOrderByCreatedAtDesc(inmateId);
    }

    public List<AssessmentAnswer> getAnswersByResponseId(Long responseId) {
        return answerRepository.findByResponseIdOrderByQuestionNo(responseId);
    }

    public Optional<AssessmentResponse> getResponseByTaskId(Long taskId) {
        return responseRepository.findByTaskId(taskId);
    }

    public String determineComprehensiveRiskLevel(Long inmateId) {
        List<AssessmentResult> results = resultRepository.findByInmateIdOrderByCreatedAtDesc(inmateId);
        if (results.isEmpty()) {
            return "未评估";
        }

        Map<String, AssessmentResult> latestByScale = new HashMap<>();
        for (AssessmentResult r : results) {
            if (r.getScaleCode() != null && !latestByScale.containsKey(r.getScaleCode())) {
                latestByScale.put(r.getScaleCode(), r);
            }
        }

        String highestLevel = "低风险";
        for (AssessmentResult r : latestByScale.values()) {
            String level = r.getRiskLevel();
            if ("高风险".equals(level)) {
                return "高风险";
            } else if ("中风险".equals(level)) {
                highestLevel = "中风险";
            }
        }
        return highestLevel;
    }
}
