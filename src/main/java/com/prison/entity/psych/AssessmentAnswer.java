package com.prison.entity.psych;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "assessment_answers")
public class AssessmentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long responseId;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private Integer questionNo;

    private Integer selectedOptionNo;

    @Column(length = 10)
    private String answerValue;

    private Integer score;
}
