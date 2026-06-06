package com.prison.entity.psych;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "assessment_responses")
public class AssessmentResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long taskId;

    @Column(nullable = false)
    private Long inmateId;

    @Column(nullable = false)
    private Long scaleId;

    @Column(nullable = false)
    private LocalDateTime submitTime;

    @Column(length = 500)
    private String remark;
}
