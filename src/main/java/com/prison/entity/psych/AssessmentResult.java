package com.prison.entity.psych;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "assessment_results")
public class AssessmentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long taskId;

    @Column(nullable = false)
    private Long inmateId;

    @Column(nullable = false)
    private Long scaleId;

    @Column(length = 50)
    private String scaleCode;

    private Double totalScore;

    private Double rawScore;

    private Double standardScore;

    @Column(length = 1000)
    private String factorScores;

    @Column(nullable = false, length = 20)
    private String riskLevel;

    @Column(length = 1000)
    private String interpretation;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
