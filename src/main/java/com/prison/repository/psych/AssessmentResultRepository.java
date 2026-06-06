package com.prison.repository.psych;

import com.prison.entity.psych.AssessmentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssessmentResultRepository extends JpaRepository<AssessmentResult, Long> {
    Optional<AssessmentResult> findByTaskId(Long taskId);
    List<AssessmentResult> findByInmateIdOrderByCreatedAtDesc(Long inmateId);
    List<AssessmentResult> findByInmateIdAndScaleCodeOrderByCreatedAtDesc(Long inmateId, String scaleCode);
    
    @Query("SELECT r.riskLevel, COUNT(r) FROM AssessmentResult r WHERE r.id IN (SELECT MAX(r2.id) FROM AssessmentResult r2 GROUP BY r2.inmateId) GROUP BY r.riskLevel")
    List<Object[]> countByRiskLevelLatest();
    
    @Query("SELECT COUNT(r) FROM AssessmentResult r WHERE r.riskLevel = :riskLevel AND r.createdAt >= :start AND r.createdAt < :end")
    long countByRiskLevelAndCreatedAtBetween(@Param("riskLevel") String riskLevel, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT FUNCTION('DATE_FORMAT', r.createdAt, '%Y-%m') as month, COUNT(r) FROM AssessmentResult r WHERE r.riskLevel = '高风险' AND r.createdAt >= :start GROUP BY month ORDER BY month")
    List<Object[]> countMonthlyHighRisk(@Param("start") LocalDateTime start);
}
