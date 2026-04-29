package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.enums.DemandeStatus;
import com.iit.internship_manager.domain.models.AcademicYear;
import com.iit.internship_manager.domain.models.Candidature;
import com.iit.internship_manager.domain.models.Sujet;

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
    List<Candidature> findBySujet(Sujet sujet);

    // --- STUDENT VIEW (Group-Aware & Year-Aware) ---
    Page<Candidature> findByGroupeMembresIdAndAnneeUniversitaire(Long studentId, AcademicYear year, Pageable pageable);

    Page<Candidature> findByGroupeMembresIdAndStatutAndAnneeUniversitaire(Long studentId, DemandeStatus statut,
            AcademicYear year, Pageable pageable);

    List<Candidature> findByGroupeMembresIdAndAnneeUniversitaire(Long studentId, AcademicYear year);

    boolean existsBySujetId(Long sujetId);
    List<Candidature> findBySujetAndStatut(Sujet sujet, DemandeStatus statut);

    // This checks if the user (student OR teacher) is linked to the candidature
    @Query("SELECT COUNT(c) > 0 FROM Candidature c WHERE c.id = :id AND " +
                    "(c.etudiant.email = :email OR c.encadrant.email = :email)")
    boolean existsByIdAndUserEmail(@Param("id") Long id, @Param("email") String email);
    // Used for automatic rejection logic in the current cycle
    List<Candidature> findByGroupeMembresIdInAndStatutInAndAnneeUniversitaire(
            List<Long> memberIds,
            List<DemandeStatus> statuts,
            AcademicYear year);

    // --- TEACHER VIEW (Year-Aware) ---
    Page<Candidature> findBySujetEnseignantIdAndAnneeUniversitaire(Long teacherId, AcademicYear year,
            Pageable pageable);

    Page<Candidature> findBySujetEnseignantIdAndStatutAndAnneeUniversitaire(Long teacherId, DemandeStatus statut,
            AcademicYear year, Pageable pageable);
@Query("SELECT c FROM Candidature c WHERE c.anneeUniversitaire = :year " +
           "AND (:status IS NULL OR c.statut = :status) " +
           "AND c.sujet.enseignant.id = :teacherId")
    Page<Candidature> findByYearAndTeacher(@Param("year") AcademicYear year, 
                                           @Param("status") DemandeStatus status, 
                                           @Param("teacherId") Long teacherId, 
                                           Pageable pageable);

    // Find other candidatures for the same subject to reject them (Year is implicit
    // via sujetId, but good for safety)
    List<Candidature> findBySujetIdAndIdNot(Long sujetId, Long acceptedCandidatureId);

    Optional<Candidature> findByGroupeIdAndStatut(Long groupeId, DemandeStatus statut);

    boolean existsByGroupeIdAndSujetId(Long groupeId, Long sujetId);
}