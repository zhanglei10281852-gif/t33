package com.prison.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "visitors")
public class Visitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long inmateId;

    @Column(nullable = false, length = 50)
    private String visitorName;

    @Column(nullable = false, length = 18)
    private String visitorIdCard;

    @Column(nullable = false, length = 20)
    private String relationship;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private LocalDate visitDate;

    @Column(nullable = false, length = 10)
    private String timeSlot;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 500)
    private String remark;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "待审批";
        }
    }
}
