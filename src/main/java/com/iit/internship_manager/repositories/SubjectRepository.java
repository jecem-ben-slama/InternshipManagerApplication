package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.enums.DepartmentType;
import com.iit.internship_manager.domain.enums.SujetStatus;
import com.iit.internship_manager.domain.models.AcademicYear;
import com.iit.internship_manager.domain.models.Sujet;
import com.iit.internship_manager.domain.models.Enseignant;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SubjectRepository extends JpaRepository<Sujet, Long> {

        @Query(value = "SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant " +
                        "WHERE s.anneeUniversitaire = :year", countQuery = "SELECT COUNT(s) FROM Sujet s WHERE s.anneeUniversitaire = :year")
        Page<Sujet> findAll(@Param("year") AcademicYear year, Pageable pageable);

        Page<Sujet> findByAnneeUniversitaire(AcademicYear year, Pageable pageable);

        Page<Sujet> findByStatutAndAnneeUniversitaire(SujetStatus status, AcademicYear year, Pageable pageable);

        // Filter by department field directly on the Sujet entity
        Page<Sujet> findByDepartmentAndAnneeUniversitaire(DepartmentType department, AcademicYear anneeUniversitaire,
                        Pageable pageable);

        @Query(value = "SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant " +
                        "WHERE s.statut = :status AND s.department = :dept AND s.anneeUniversitaire = :year", countQuery = "SELECT COUNT(s) FROM Sujet s WHERE s.statut = :status AND s.department = :dept AND s.anneeUniversitaire = :year")
        Page<Sujet> findByStatutAndDepartmentAndAnneeUniversitaire(
                        @Param("status") SujetStatus status,
                        @Param("dept") DepartmentType dept,
                        @Param("year") AcademicYear year,
                        Pageable pageable);

        Page<Sujet> findByEnseignantId(Long teacherId, Pageable pageable);

        @Query(value = "SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant " +
                        "WHERE s.enseignant.id = :teacherId AND s.anneeUniversitaire = :year", countQuery = "SELECT COUNT(s) FROM Sujet s WHERE s.enseignant.id = :teacherId AND s.anneeUniversitaire = :year")
        Page<Sujet> findByEnseignantIdAndAnneeUniversitaire(
                        @Param("teacherId") Long teacherId,
                        @Param("year") AcademicYear year,
                        Pageable pageable);

        List<Sujet> findByEnseignantAndAnneeUniversitaire(Enseignant teacher, AcademicYear year);

        @Query("SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant WHERE s.id = :id")
        Optional<Sujet> findByIdWithDetails(@Param("id") Long id);

        @Modifying
        @Transactional
        @Query("UPDATE Sujet s SET s.statut = 'TAKEN' " +
                        "WHERE s.enseignant.id = :teacherId " +
                        "AND s.statut = 'AVAILABLE' " +
                        "AND s.anneeUniversitaire.active = true")
        void markAllSubjectsAsTakenForTeacher(@Param("teacherId") Long teacherId);
}