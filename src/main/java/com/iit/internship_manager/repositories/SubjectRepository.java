package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.enums.SujetStatus;
import com.iit.internship_manager.domain.models.Sujet;
import org.springframework.data.domain.Page; // Add this import
import org.springframework.data.domain.Pageable; // Add this import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Sujet, Long> {

    // Changing return type from List to Page
    Page<Sujet> findByStatut(SujetStatus statut, Pageable pageable);

    // Changing return type from List to Page
    Page<Sujet> findByProposantId(Long enseignantId, Pageable pageable);
}