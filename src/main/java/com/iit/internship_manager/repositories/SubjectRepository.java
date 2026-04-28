package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.enums.DepartmentType;
import com.iit.internship_manager.domain.enums.SujetStatus;
import com.iit.internship_manager.domain.models.Sujet;

import java.util.Optional;

import org.springframework.data.domain.Page; // Add this import
import org.springframework.data.domain.Pageable; // Add this import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


    @Repository
    public interface SubjectRepository extends JpaRepository<Sujet, Long> {

        // Overriding findAll to fetch both types of creators
        @Query(value = "SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant", countQuery = "SELECT COUNT(s) FROM Sujet s")
        Page<Sujet> findAll(Pageable pageable);

        @Query(value = "SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant WHERE s.statut = :statut", countQuery = "SELECT COUNT(s) FROM Sujet s WHERE s.statut = :statut")
        Page<Sujet> findByStatut(@Param("statut") SujetStatus statut, Pageable pageable);

        @Query(value = "SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant WHERE s.enseignant.id = :teacherId", countQuery = "SELECT COUNT(s) FROM Sujet s WHERE s.enseignant.id = :teacherId")
        Page<Sujet> findByEnseignantId(@Param("teacherId") Long teacherId, Pageable pageable);

        @Query(value = "SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant " +
                "WHERE s.enseignant.department = :dept AND s.statut = :statut", countQuery = "SELECT COUNT(s) FROM Sujet s WHERE s.enseignant.department = :dept AND s.statut = :statut")
        Page<Sujet> findByDepartmentAndStatut(@Param("dept") DepartmentType dept, @Param("statut") SujetStatus statut,
                Pageable pageable);
                
        @Query("SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant WHERE s.id = :id")
        Optional<Sujet> findByIdWithDetails(@Param("id") Long id);
    }
