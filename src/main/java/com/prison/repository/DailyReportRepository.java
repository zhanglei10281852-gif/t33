package com.prison.repository;

import com.prison.entity.DailyReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {
    Page<DailyReport> findByInmateId(Long inmateId, Pageable pageable);

    List<DailyReport> findByInmateIdAndReportDateBetween(Long inmateId, LocalDate start, LocalDate end);

    List<DailyReport> findByReportDate(LocalDate date);

    boolean existsByInmateIdAndReportDate(Long inmateId, LocalDate date);
}
