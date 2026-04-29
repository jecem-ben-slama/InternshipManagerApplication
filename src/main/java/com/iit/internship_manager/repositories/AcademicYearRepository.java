package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.models.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, String> {

    /**
     * Retrieves the single academic year marked as active.
     * This is the "Source of Truth" for your default logic.
     */
    Optional<AcademicYear> findByActiveTrue();
    Optional<AcademicYear> findByIdAndActiveTrue(String id);
    Optional<AcademicYear> findById(String id);
    /**
     * Checks if an academic year with a specific ID exists and is active.
     */
    boolean existsByIdAndActiveTrue(String id);

    /**
     * Useful for administrative checks to ensure only one year is active.
     */
    long countByActiveTrue();
}