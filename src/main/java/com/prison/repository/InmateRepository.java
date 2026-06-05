package com.prison.repository;

import com.prison.entity.Inmate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface InmateRepository extends JpaRepository<Inmate, Long> {
    Optional<Inmate> findByInmateNo(String inmateNo);

    Page<Inmate> findByWard(String ward, Pageable pageable);

    Page<Inmate> findByStatus(String status, Pageable pageable);

    @Query("SELECT i FROM Inmate i WHERE i.name LIKE %:keyword% OR i.inmateNo LIKE %:keyword%")
    Page<Inmate> search(@Param("keyword") String keyword, Pageable pageable);

    long countByStatus(String status);

    long countByWard(String ward);
}
