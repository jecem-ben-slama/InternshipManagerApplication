package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.models.Affectation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Long> {
    // You can add findByGroupeId or findByEncadrantId here later for reports
    // Check if any official affectation exists for a specific student ID
    boolean existsByGroupeMembresId(Long studentId);
}