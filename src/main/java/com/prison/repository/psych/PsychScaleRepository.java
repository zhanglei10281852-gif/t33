package com.prison.repository.psych;

import com.prison.entity.psych.PsychScale;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface PsychScaleRepository extends JpaRepository<PsychScale, Long> {
    Optional<PsychScale> findByCode(String code);
    List<PsychScale> findByScaleType(String scaleType);
    Optional<PsychScale> findTopByCodeOrderByVersionDesc(String code);
}
