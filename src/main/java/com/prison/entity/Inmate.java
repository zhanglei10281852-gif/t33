package com.prison.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "inmates")
public class Inmate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String inmateNo;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 18)
    private String idCard;

    @Column(nullable = false, length = 4)
    private String gender;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, length = 100)
    private String crime;

    @Column(nullable = false)
    private Integer sentenceMonths;

    @Column(nullable = false)
    private LocalDate admissionDate;

    private LocalDate expectedRelease;

    @Column(nullable = false, length = 20)
    private String ward;

    @Column(nullable = false, length = 10)
    private String cellNo;

    @Column(nullable = false, length = 20)
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "在押";
        }
        if (this.expectedRelease == null && this.admissionDate != null && this.sentenceMonths != null) {
            this.expectedRelease = this.admissionDate.plusMonths(this.sentenceMonths);
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
