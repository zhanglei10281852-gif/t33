package com.prison.service.psych;

import com.prison.dto.psych.CrisisInterventionDTO;
import com.prison.entity.psych.CrisisIntervention;
import com.prison.repository.psych.CrisisInterventionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CrisisInterventionService {

    private final CrisisInterventionRepository interventionRepository;

    @Transactional
    public CrisisIntervention autoGenerateIntervention(Long inmateId, Long taskId, Long resultId, String riskLevel) {
        CrisisIntervention intervention = new CrisisIntervention();
        intervention.setInmateId(inmateId);
        intervention.setTaskId(taskId);
        intervention.setResultId(resultId);
        intervention.setRiskLevel(riskLevel);
        intervention.setStatus("待干预");

        if ("高风险".equals(riskLevel)) {
            intervention.setUrgency("紧急");
        } else {
            intervention.setUrgency("一般");
        }

        return interventionRepository.save(intervention);
    }

    public Page<CrisisIntervention> listInterventions(int page, int size, String status, String counselor, String urgency) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (status != null && !status.isBlank()) {
            return interventionRepository.findByStatus(status, pageable);
        }
        if (counselor != null && !counselor.isBlank()) {
            return interventionRepository.findByAssignedCounselor(counselor, pageable);
        }
        return interventionRepository.findAll(pageable);
    }

    public Optional<CrisisIntervention> getInterventionById(Long id) {
        return interventionRepository.findById(id);
    }

    public List<CrisisIntervention> getInterventionsByInmate(Long inmateId) {
        return interventionRepository.findByInmateIdOrderByCreatedAtDesc(inmateId);
    }

    @Transactional
    public CrisisIntervention createIntervention(CrisisInterventionDTO dto) {
        CrisisIntervention intervention = new CrisisIntervention();
        intervention.setInmateId(dto.getInmateId());
        intervention.setTaskId(dto.getTaskId());
        intervention.setResultId(dto.getResultId());
        intervention.setUrgency(dto.getUrgency() != null ? dto.getUrgency() : "一般");
        intervention.setAssignedCounselor(dto.getAssignedCounselor());
        intervention.setRiskLevel(dto.getRiskLevel());
        if (dto.getInterventionDate() != null) {
            intervention.setInterventionDate(LocalDate.parse(dto.getInterventionDate()));
        }
        intervention.setInterventionMethod(dto.getInterventionMethod());
        intervention.setInterventionContent(dto.getInterventionContent());
        intervention.setPostInterventionAssessment(dto.getPostInterventionAssessment());
        intervention.setRiskReduced(dto.getRiskReduced());
        intervention.setFollowUpPlan(dto.getFollowUpPlan());
        if (dto.getFollowUpDate() != null) {
            intervention.setFollowUpDate(LocalDate.parse(dto.getFollowUpDate()));
        }
        return interventionRepository.save(intervention);
    }

    @Transactional
    public CrisisIntervention updateIntervention(Long id, CrisisInterventionDTO dto) {
        return interventionRepository.findById(id).map(intervention -> {
            if (dto.getAssignedCounselor() != null) {
                intervention.setAssignedCounselor(dto.getAssignedCounselor());
            }
            if (dto.getUrgency() != null) {
                intervention.setUrgency(dto.getUrgency());
            }
            if (dto.getInterventionDate() != null) {
                intervention.setInterventionDate(LocalDate.parse(dto.getInterventionDate()));
            }
            if (dto.getInterventionMethod() != null) {
                intervention.setInterventionMethod(dto.getInterventionMethod());
            }
            if (dto.getInterventionContent() != null) {
                intervention.setInterventionContent(dto.getInterventionContent());
            }
            if (dto.getPostInterventionAssessment() != null) {
                intervention.setPostInterventionAssessment(dto.getPostInterventionAssessment());
            }
            if (dto.getRiskReduced() != null) {
                intervention.setRiskReduced(dto.getRiskReduced());
            }
            if (dto.getFollowUpPlan() != null) {
                intervention.setFollowUpPlan(dto.getFollowUpPlan());
            }
            if (dto.getFollowUpDate() != null) {
                intervention.setFollowUpDate(LocalDate.parse(dto.getFollowUpDate()));
            }
            if (intervention.getInterventionDate() != null && intervention.getInterventionContent() != null) {
                intervention.setStatus("已完成");
            }
            return interventionRepository.save(intervention);
        }).orElse(null);
    }

    @Transactional
    public CrisisIntervention updateStatus(Long id, String status) {
        return interventionRepository.findById(id).map(intervention -> {
            intervention.setStatus(status);
            return interventionRepository.save(intervention);
        }).orElse(null);
    }

    @Transactional
    public void deleteIntervention(Long id) {
        interventionRepository.deleteById(id);
    }
}
