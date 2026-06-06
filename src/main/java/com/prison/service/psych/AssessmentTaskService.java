package com.prison.service.psych;

import com.prison.dto.psych.AssessmentTaskDTO;
import com.prison.entity.Inmate;
import com.prison.entity.psych.AssessmentTask;
import com.prison.entity.psych.PsychScale;
import com.prison.repository.InmateRepository;
import com.prison.repository.psych.AssessmentTaskRepository;
import com.prison.repository.psych.PsychScaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssessmentTaskService {

    private final AssessmentTaskRepository taskRepository;
    private final InmateRepository inmateRepository;
    private final PsychScaleRepository scaleRepository;

    public Page<AssessmentTask> listTasks(int page, int size, String status, Long inmateId, String taskType) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (inmateId != null) {
            return taskRepository.findByInmateId(inmateId, pageable);
        }
        if (status != null && !status.isBlank()) {
            return taskRepository.findByStatus(status, pageable);
        }
        return taskRepository.findAll(pageable);
    }

    public Optional<AssessmentTask> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    @Transactional
    public AssessmentTask createTask(AssessmentTaskDTO dto) {
        AssessmentTask task = new AssessmentTask();
        task.setInmateId(dto.getInmateId());
        task.setScaleId(dto.getScaleId());
        task.setTaskType(dto.getTaskType() != null ? dto.getTaskType() : "人工安排");
        task.setPlannedDate(LocalDate.parse(dto.getPlannedDate()));
        task.setAssignedCounselor(dto.getAssignedCounselor());
        task.setRemark(dto.getRemark());
        task.setStatus("待完成");
        return taskRepository.save(task);
    }

    @Transactional
    public AssessmentTask updateTaskStatus(Long id, String status) {
        return taskRepository.findById(id).map(task -> {
            task.setStatus(status);
            if ("已完成".equals(status)) {
                task.setCompletedAt(LocalDateTime.now());
            }
            return taskRepository.save(task);
        }).orElse(null);
    }

    @Transactional
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public List<AssessmentTask> getPendingTasks(Long inmateId) {
        return taskRepository.findByInmateIdAndStatus(inmateId, "待完成");
    }

    public Page<AssessmentTask> getOverdueTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("plannedDate").ascending());
        return taskRepository.findByStatusAndPlannedDateBefore("待完成", LocalDate.now(), pageable);
    }

    @Transactional
    public void generateIntakeAssessment(Long inmateId) {
        Optional<Inmate> inmateOpt = inmateRepository.findById(inmateId);
        if (inmateOpt.isEmpty()) return;

        Inmate inmate = inmateOpt.get();
        LocalDate plannedDate = inmate.getAdmissionDate().plusDays(7);

        String[] scaleCodes = {"SCL90", "SDS", "SAS"};
        for (String code : scaleCodes) {
            Optional<PsychScale> scaleOpt = scaleRepository.findTopByCodeOrderByVersionDesc(code);
            if (scaleOpt.isPresent()) {
                PsychScale scale = scaleOpt.get();
                
                List<AssessmentTask> existing = taskRepository.findPendingByInmateAndScaleAndType(
                        inmateId, scale.getId(), "入监评估");
                if (existing.isEmpty()) {
                    AssessmentTask task = new AssessmentTask();
                    task.setInmateId(inmateId);
                    task.setScaleId(scale.getId());
                    task.setTaskType("入监评估");
                    task.setPlannedDate(plannedDate);
                    task.setStatus("待完成");
                    task.setRemark("新入监自动生成评估任务");
                    taskRepository.save(task);
                }
            }
        }
    }

    @Transactional
    public void generateQuarterlyAssessments() {
        List<Inmate> inmates = inmateRepository.findByStatus("在押", Pageable.unpaged()).getContent();
        LocalDate today = LocalDate.now();

        Optional<PsychScale> scl90Opt = scaleRepository.findTopByCodeOrderByVersionDesc("SCL90");
        if (scl90Opt.isEmpty()) return;
        Long scl90Id = scl90Opt.get().getId();

        for (Inmate inmate : inmates) {
            LocalDate admissionDate = inmate.getAdmissionDate();
            long monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(admissionDate, today);
            
            if (monthsBetween >= 3) {
                int quarters = (int) (monthsBetween / 3);
                LocalDate expectedQuarterDate = admissionDate.plusMonths(quarters * 3L);
                
                if (!expectedQuarterDate.isAfter(today.plusDays(7))) {
                    List<AssessmentTask> existing = taskRepository.findPendingByInmateAndScaleAndType(
                            inmate.getId(), scl90Id, "定期评估");
                    if (existing.isEmpty()) {
                        List<AssessmentTask> allQuarterly = taskRepository.findByInmateIdAndScaleIdAndTaskType(
                                inmate.getId(), scl90Id, "定期评估");
                        boolean alreadyHasThisQuarter = allQuarterly.stream()
                                .anyMatch(t -> !t.getPlannedDate().isBefore(expectedQuarterDate.minusDays(1)) 
                                            && !t.getPlannedDate().isAfter(expectedQuarterDate.plusDays(1)));
                        
                        if (!alreadyHasThisQuarter) {
                            AssessmentTask task = new AssessmentTask();
                            task.setInmateId(inmate.getId());
                            task.setScaleId(scl90Id);
                            task.setTaskType("定期评估");
                            task.setPlannedDate(expectedQuarterDate);
                            task.setStatus("待完成");
                            task.setRemark("季度定期评估自动生成");
                            taskRepository.save(task);
                        }
                    }
                }
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void scheduledGenerateAssessments() {
        generateQuarterlyAssessments();
    }
}
