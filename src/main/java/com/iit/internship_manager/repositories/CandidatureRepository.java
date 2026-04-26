package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.enums.DemandeStatus;
import com.iit.internship_manager.domain.models.Candidature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    // --- 1. VALIDATION HELPER ---
    boolean existsByEtudiantIdAndSujetId(Long etudiantId, Long sujetId);

    // --- 2. PAGINATED RETRIEVAL (With & Without Status) ---

    // For the Student:
    Page<Candidature> findByEtudiantId(Long studentId, Pageable pageable);

    Page<Candidature> findByEtudiantIdAndStatut(Long studentId, DemandeStatus statut, Pageable pageable);

    // For the Teacher (Looking at their proposed subjects):
    Page<Candidature> findBySujetProposantId(Long teacherId, Pageable pageable);

    Page<Candidature> findBySujetProposantIdAndStatut(Long teacherId, DemandeStatus statut, Pageable pageable);

    Page<Candidature> findBySujetEnseignantId(Long teacherId, Pageable pageable);

    Page<Candidature> findBySujetEnseignantIdAndStatut(Long teacherId, DemandeStatus statut, Pageable pageable);
    List<Candidature> findByEtudiantId(Long studentId);
    // --- 3. CLEANUP HELPER ---
    // Used when one student is accepted, to reject everyone else for that subject
    List<Candidature> findBySujetIdAndIdNot(Long sujetId, Long acceptedCandidatureId);
}