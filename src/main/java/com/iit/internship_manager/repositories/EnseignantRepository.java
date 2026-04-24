package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.models.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EnseignantRepository extends JpaRepository<Enseignant, Long> {
    // This will help us find the PFE coordinator quickly
    Optional<Enseignant> findByIsResponsablePFETrue();
}