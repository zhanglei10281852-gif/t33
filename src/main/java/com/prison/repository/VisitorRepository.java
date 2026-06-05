package com.prison.repository;

import com.prison.entity.Visitor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    Page<Visitor> findByInmateId(Long inmateId, Pageable pageable);

    List<Visitor> findByVisitDateAndStatus(LocalDate date, String status);

    Page<Visitor> findByStatus(String status, Pageable pageable);

    long countByVisitDate(LocalDate date);
}
