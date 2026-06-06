package com.prison.entity.psych;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "psych_scale_questions")
public class PsychScaleQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long scaleId;

    @Column(nullable = false)
    private Integer questionNo;

    @Column(nullable = false, length = 500)
    private String questionText;

    @Column(nullable = false, length = 20)
    private String questionType;

    private Boolean reverseScoring;

    @Column(length = 20)
    private String factorName;
}
