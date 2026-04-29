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

        // All subjects for a given year (with eager fetch for teacher & proposant)
        @Query(value = "SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant " +
                        "WHERE s.anneeUniversitaire = :year", countQuery = "SELECT COUNT(s) FROM Sujet s WHERE s.anneeUniversitaire = :year")
        Page<Sujet> findAll(@Param("year") AcademicYear year, Pageable pageable);

        // Subjects by year (no fetch join — used by archive/simple queries)
        Page<Sujet> findByAnneeUniversitaire(AcademicYear year, Pageable pageable);

        // Subjects by status + year (derived query — replaces the old @Query
        // findByStatut)
        Page<Sujet> findByStatutAndAnneeUniversitaire(SujetStatus status, AcademicYear year, Pageable pageable);

        // Subjects by status + department + year
        // Replaces both findByStatutAndDepartment and
        // findByStatutAndDepartmentAndAnneeUniversitaire
        @Query(value = "SELECT s FROM Sujet s JOIN FETCH s.enseignant e LEFT JOIN FETCH s.proposant " +
                        "WHERE s.statut = :status AND e.department = :dept AND s.anneeUniversitaire = :year", countQuery = "SELECT COUNT(s) FROM Sujet s JOIN s.enseignant e "
                                        +
                                        "WHERE s.statut = :status AND e.department = :dept AND s.anneeUniversitaire = :year")
        Page<Sujet> findByStatutAndDepartmentAndAnneeUniversitaire(
                        @Param("status") SujetStatus status,
                        @Param("dept") DepartmentType dept,
                        @Param("year") AcademicYear year,
                        Pageable pageable);

        // Subjects by department + any status + year (teacher's dept view)
        @Query(value = "SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant " +
                        "WHERE s.enseignant.department = :dept AND s.statut = :statut AND s.anneeUniversitaire = :year", countQuery = "SELECT COUNT(s) FROM Sujet s "
                                        +
                                        "WHERE s.enseignant.department = :dept AND s.statut = :statut AND s.anneeUniversitaire = :year")
        Page<Sujet> findByDepartmentAndStatut(
                        @Param("dept") DepartmentType dept,
                        @Param("statut") SujetStatus statut,
                        @Param("year") AcademicYear year,
                        Pageable pageable);

        // Teacher's subjects — no year filter (full history, used by
        // getSubjectsByCurrentTeacher)
        Page<Sujet> findByEnseignantId(Long teacherId, Pageable pageable);

        // Teacher's subjects for a specific year
        // Replaces the old @Query findByEnseignantId(Long, AcademicYear, Pageable)
        @Query(value = "SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant " +
                        "WHERE s.enseignant.id = :teacherId AND s.anneeUniversitaire = :year", countQuery = "SELECT COUNT(s) FROM Sujet s WHERE s.enseignant.id = :teacherId AND s.anneeUniversitaire = :year")
        Page<Sujet> findByEnseignantIdAndAnneeUniversitaire(
                        @Param("teacherId") Long teacherId,
                        @Param("year") AcademicYear year,
                        Pageable pageable);

        // All subjects for a teacher in a given year as a flat list (used by
        // abortAffectation)
        List<Sujet> findByEnseignantAndAnneeUniversitaire(Enseignant teacher, AcademicYear year);

        // Single subject with full details
        @Query("SELECT s FROM Sujet s LEFT JOIN FETCH s.enseignant LEFT JOIN FETCH s.proposant WHERE s.id = :id")
        Optional<Sujet> findByIdWithDetails(@Param("id") Long id);

        // Quota auto-lock: mark all available subjects as TAKEN for the active year
        @Modifying
        @Transactional
        @Query("UPDATE Sujet s SET s.statut = 'TAKEN' " +
                        "WHERE s.enseignant.id = :teacherId " +
                        "AND s.statut = 'AVAILABLE' " +
                        "AND s.anneeUniversitaire.active = true")
        void markAllSubjectsAsTakenForTeacher(@Param("teacherId") Long teacherId);
}