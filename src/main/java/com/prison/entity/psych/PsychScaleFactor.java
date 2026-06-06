package com.prison.entity.psych;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "psych_scale_factors")
public class PsychScaleFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long scaleId;

    @Column(nullable = false, length = 50)
    private String factorName;

    @Column(nullable = false, length = 500)
    private String questionNos;

    @Column(length = 500)
    private String description;
}
