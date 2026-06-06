package com.prison.entity.psych;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "assessment_tasks")
public class AssessmentTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long inmateId;

    @Column(nullable = false)
    private Long scaleId;

    @Column(nullable = false, length = 20)
    private String taskType;

    @Column(nullable = false)
    private LocalDate plannedDate;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 100)
    private String assignedCounselor;

    @Column(length = 500)
    private String remark;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "待完成";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
