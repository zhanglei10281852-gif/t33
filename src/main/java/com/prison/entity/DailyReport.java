package com.prison.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "daily_reports")
public class DailyReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long inmateId;

    @Column(nullable = false)
    private LocalDate reportDate;

    @Column(nullable = false, length = 20)
    private String healthStatus;

    @Column(length = 500)
    private String behaviorNote;

    @Column(nullable = false, length = 20)
    private String moodLevel;

    @Column(length = 50)
    private String reporter;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
