package com.prison.repository.psych;

import com.prison.entity.psych.CrisisIntervention;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CrisisInterventionRepository extends JpaRepository<CrisisIntervention, Long> {
    Page<CrisisIntervention> findByStatus(String status, Pageable pageable);
    List<CrisisIntervention> findByInmateIdOrderByCreatedAtDesc(Long inmateId);
    Page<CrisisIntervention> findByAssignedCounselor(String counselor, Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM CrisisIntervention c WHERE c.riskReduced = true AND c.status = '已完成'")
    long countEffectiveInterventions();
    
    @Query("SELECT COUNT(c) FROM CrisisIntervention c WHERE c.status = '已完成'")
    long countCompletedInterventions();
    
    long countByStatus(String status);
}
