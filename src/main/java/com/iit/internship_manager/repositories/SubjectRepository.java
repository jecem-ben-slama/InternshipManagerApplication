package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.enums.DepartmentType;
import com.iit.internship_manager.domain.enums.SujetStatus;
import com.iit.internship_manager.domain.models.AcademicYear;
import com.iit.internship_manager.domain.models.Sujet;

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

        // 1. Restored original findAll with Year filter
        @Query(value = "SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant " +
                        "WHERE s.anneeUniversitaire = :year", countQuery = "SELECT COUNT(s) FROM Sujet s WHERE s.anneeUniversitaire = :year")
        Page<Sujet> findAll(@Param("year") AcademicYear year, Pageable pageable);

        // 2. Restored findByStatut with Year filter
        @Query(value = "SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant " +
                        "WHERE s.statut = :statut AND s.anneeUniversitaire = :year", countQuery = "SELECT COUNT(s) FROM Sujet s WHERE s.statut = :statut AND s.anneeUniversitaire = :year")
        Page<Sujet> findByStatut(@Param("statut") SujetStatus statut, @Param("year") AcademicYear year,
                        Pageable pageable);

        // 3. Restored findByEnseignantId with Year filter
        @Query(value = "SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant " +
                        "WHERE s.enseignant.id = :teacherId AND s.anneeUniversitaire = :year", countQuery = "SELECT COUNT(s) FROM Sujet s WHERE s.enseignant.id = :teacherId AND s.anneeUniversitaire = :year")
        Page<Sujet> findByEnseignantId(@Param("teacherId") Long teacherId, @Param("year") AcademicYear year,
                        Pageable pageable);

        // 4. Restored findByDepartmentAndStatut (Teacher's Dept) with Year filter
        @Query(value = "SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant " +
                        "WHERE s.enseignant.department = :dept AND s.statut = :statut AND s.anneeUniversitaire = :year", countQuery = "SELECT COUNT(s) FROM Sujet s WHERE s.enseignant.department = :dept AND s.statut = :statut AND s.anneeUniversitaire = :year")
        Page<Sujet> findByDepartmentAndStatut(@Param("dept") DepartmentType dept, @Param("statut") SujetStatus statut,
                        @Param("year") AcademicYear year, Pageable pageable);

        // 5. Restored findByStatutAndDepartment (Specific Sujet Join) with Year filter
        @Query(value = "SELECT s FROM Sujet s " +
                        "JOIN FETCH s.enseignant e " +
                        "LEFT JOIN FETCH s.proposant " +
                        "WHERE s.statut = :status " +
                        "AND e.department = :dept " +
                        "AND s.anneeUniversitaire = :year", countQuery = "SELECT COUNT(s) FROM Sujet s JOIN s.enseignant e "
                                        +
                                        "WHERE s.statut = :status AND e.department = :dept AND s.anneeUniversitaire = :year")
        Page<Sujet> findByStatutAndDepartment(
                        @Param("status") SujetStatus status,
                        @Param("dept") DepartmentType dept,
                        @Param("year") AcademicYear year,
                        Pageable pageable);

        Page<Sujet> findByAnneeUniversitaire(AcademicYear year, Pageable pageable);

        // 6. Restored your specific Modifying Query with active year safety
        @Modifying
        @Transactional
        @Query("UPDATE Sujet s SET s.statut = 'TAKEN' " +
                        "WHERE s.enseignant.id = :teacherId " +
                        "AND s.statut = 'AVAILABLE' " +
                        "AND s.anneeUniversitaire.active = true")
        void markAllSubjectsAsTakenForTeacher(@Param("teacherId") Long teacherId);
        
        // Fixes "The method findByStatutAndDepartmentAndAnneeUniversitaire(...) is
        // undefined"
        @Query("SELECT s FROM Sujet s JOIN FETCH s.enseignant e WHERE s.statut = :status " +
                        "AND e.department = :dept AND s.anneeUniversitaire = :year")
        Page<Sujet> findByStatutAndDepartmentAndAnneeUniversitaire(
                        @Param("status") SujetStatus status,
                        @Param("dept") DepartmentType dept,
                        @Param("year") AcademicYear year,
                        Pageable pageable);

        // Fixes "The method findByStatutAndAnneeUniversitaire(...) is undefined"
        Page<Sujet> findByStatutAndAnneeUniversitaire(SujetStatus status, AcademicYear year, Pageable pageable);

        // Fixes "The method findByEnseignantId(Long, AcademicYear, Pageable) ... is not
        // applicable"
        // We update the signature to accept the year
        Page<Sujet> findByEnseignantId(Long teacherId, Pageable pageable);

        // Add this one if you want current-year only for teachers
        @Query("SELECT s FROM Sujet s WHERE s.enseignant.id = :teacherId AND s.anneeUniversitaire = :year")
        Page<Sujet> findByEnseignantIdAndAnneeUniversitaire(Long teacherId, AcademicYear year, Pageable pageable);

        @Query("SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant WHERE s.id = :id")
        Optional<Sujet> findByIdWithDetails(@Param("id") Long id);
}