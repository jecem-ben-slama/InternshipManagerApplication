package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.enums.DemandeStatus;
import com.iit.internship_manager.domain.models.Candidature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    // Check if any member of a group has already applied for this subject
    @Query("SELECT COUNT(c) > 0 FROM Candidature c JOIN c.groupe.membres m WHERE m.id = :studentId AND c.sujet.id = :sujetId")
    boolean existsByStudentInGroupAndSujetId(@Param("studentId") Long studentId, @Param("sujetId") Long sujetId);

    // --- STUDENT VIEW (Group-Aware) ---
    Page<Candidature> findByGroupeMembresId(Long studentId, Pageable pageable);

    Page<Candidature> findByGroupeMembresIdAndStatut(Long studentId, DemandeStatus statut, Pageable pageable);

    List<Candidature> findByGroupeMembresId(Long studentId);

    boolean existsBySujetId(Long sujetId);
    List<Candidature> findByGroupeMembresIdInAndStatutIn(
            List<Long> memberIds,
            List<DemandeStatus> statuts);
    // --- TEACHER VIEW ---
    Page<Candidature> findBySujetEnseignantId(Long teacherId, Pageable pageable);

    Page<Candidature> findBySujetEnseignantIdAndStatut(Long teacherId, DemandeStatus statut, Pageable pageable);

    // Find other candidatures for the same subject to reject them
    List<Candidature> findBySujetIdAndIdNot(Long sujetId, Long acceptedCandidatureId);
    
 
    Optional<Candidature> findByGroupeIdAndStatut(Long groupeId, DemandeStatus statut);
    
    boolean existsByGroupeIdAndSujetId(Long groupeId, Long sujetId);
}