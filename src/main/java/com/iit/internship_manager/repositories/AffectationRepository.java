package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.models.Affectation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AffectationRepository extends JpaRepository<Affectation, Long> {

    // 1. Check if student has any assignment (Fast check)
    @Query("SELECT COUNT(a) > 0 FROM Affectation a JOIN a.groupe g JOIN g.membres m WHERE m.id = :studentId")
    boolean existsByStudentId(@Param("studentId") Long studentId);

    // 2. Fetch for Responsable/Teacher View (Optimized with JOIN FETCH)
    // Note: countQuery is needed for Pageable when using JOIN FETCH
    @Query(value = "SELECT a FROM Affectation a " +
            "JOIN FETCH a.groupe g " +
            "JOIN FETCH g.membres " +
            "JOIN FETCH a.encadrant " +
            "JOIN FETCH a.sujet " +
            "WHERE a.encadrant.id = :teacherId", countQuery = "SELECT COUNT(a) FROM Affectation a WHERE a.encadrant.id = :teacherId")
    Page<Affectation> findByEncadrantId(@Param("teacherId") Long teacherId, Pageable pageable);

    // 3. Keep a non-pageable version for internal logic (like the Student conflict
    // check)
    @Query("SELECT a FROM Affectation a JOIN a.groupe g JOIN g.membres m WHERE m.id = :studentId")
    List<Affectation> findByStudentId(@Param("studentId") Long studentId);

    // 4. Find by specific group
    Optional<Affectation> findByGroupeId(Long groupeId);

    // 5. Non-pageable version for Enseignant (used in some list mappings)
    @Query("SELECT a FROM Affectation a " +
            "JOIN FETCH a.groupe g " +
            "JOIN FETCH g.membres " +
            "JOIN FETCH a.encadrant " +
            "JOIN FETCH a.sujet " +
            "WHERE a.encadrant.id = :teacherId")
    List<Affectation> findByEncadrantId(@Param("teacherId") Long teacherId);
    
    boolean existsByGroupeMembresId(Long studentId);

    // This is the custom query version we added earlier

}