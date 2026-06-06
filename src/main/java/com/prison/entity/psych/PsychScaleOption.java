package com.prison.entity.psych;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "psych_scale_options")
public class PsychScaleOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private Integer optionNo;

    @Column(nullable = false, length = 100)
    private String optionText;

    @Column(nullable = false)
    private Integer score;
}
