package com.prison.repository.psych;

import com.prison.entity.psych.PsychScaleFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PsychScaleFactorRepository extends JpaRepository<PsychScaleFactor, Long> {
    List<PsychScaleFactor> findByScaleId(Long scaleId);
    void deleteByScaleId(Long scaleId);
}
