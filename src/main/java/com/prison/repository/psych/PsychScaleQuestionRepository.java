package com.prison.repository.psych;

import com.prison.entity.psych.PsychScaleQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PsychScaleQuestionRepository extends JpaRepository<PsychScaleQuestion, Long> {
    List<PsychScaleQuestion> findByScaleIdOrderByQuestionNo(Long scaleId);
    long countByScaleId(Long scaleId);
}
