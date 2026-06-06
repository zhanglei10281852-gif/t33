package com.prison.entity.psych;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "crisis_interventions")
public class CrisisIntervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long inmateId;

    private Long taskId;

    private Long resultId;

    @Column(nullable = false, length = 20)
    private String urgency;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 50)
    private String assignedCounselor;

    @Column(length = 20)
    private String riskLevel;

    private LocalDate interventionDate;

    @Column(length = 50)
    private String interventionMethod;

    @Column(length = 2000)
    private String interventionContent;

    @Column(length = 500)
    private String postInterventionAssessment;

    private Boolean riskReduced;

    @Column(length = 500)
    private String followUpPlan;

    private LocalDate followUpDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "待干预";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
