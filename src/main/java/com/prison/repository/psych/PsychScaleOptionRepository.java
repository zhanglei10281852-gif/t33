package com.prison.repository.psych;

import com.prison.entity.psych.PsychScaleOption;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PsychScaleOptionRepository extends JpaRepository<PsychScaleOption, Long> {
    List<PsychScaleOption> findByQuestionIdOrderByOptionNo(Long questionId);
    void deleteByQuestionId(Long questionId);
}
