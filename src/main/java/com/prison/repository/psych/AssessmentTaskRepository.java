package com.prison.repository.psych;

import com.prison.entity.psych.AssessmentTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AssessmentTaskRepository extends JpaRepository<AssessmentTask, Long> {
    Page<AssessmentTask> findByInmateId(Long inmateId, Pageable pageable);
    List<AssessmentTask> findByInmateIdAndStatus(Long inmateId, String status);
    Page<AssessmentTask> findByStatus(String status, Pageable pageable);
    Page<AssessmentTask> findByStatusAndPlannedDateBefore(String status, LocalDate date, Pageable pageable);
    List<AssessmentTask> findByInmateIdAndScaleIdAndTaskType(Long inmateId, Long scaleId, String taskType);
    
    @Query("SELECT t FROM AssessmentTask t WHERE t.status = :status AND t.plannedDate < :date")
    List<AssessmentTask> findOverdueTasks(@Param("status") String status, @Param("date") LocalDate date);
    
    @Query("SELECT t FROM AssessmentTask t WHERE t.inmateId = :inmateId AND t.scaleId = :scaleId AND t.taskType = :taskType AND t.status = '待完成'")
    List<AssessmentTask> findPendingByInmateAndScaleAndType(@Param("inmateId") Long inmateId, @Param("scaleId") Long scaleId, @Param("taskType") String taskType);
    
    long countByStatus(String status);
    
    @Query("SELECT COUNT(t) FROM AssessmentTask t WHERE t.status = '待完成' AND t.plannedDate < :date")
    long countOverdue(@Param("date") LocalDate date);
}
