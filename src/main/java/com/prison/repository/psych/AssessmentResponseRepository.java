package com.prison.repository.psych;

import com.prison.entity.psych.AssessmentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface AssessmentResponseRepository extends JpaRepository<AssessmentResponse, Long> {
    Optional<AssessmentResponse> findByTaskId(Long taskId);
    List<AssessmentResponse> findByInmateIdOrderBySubmitTimeDesc(Long inmateId);
    List<AssessmentResponse> findByInmateIdAndScaleIdOrderBySubmitTimeDesc(Long inmateId, Long scaleId);
}
