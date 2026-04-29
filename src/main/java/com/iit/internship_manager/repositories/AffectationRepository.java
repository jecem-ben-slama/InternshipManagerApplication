package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.enums.DepartmentType;
import com.iit.internship_manager.domain.models.AcademicYear;
import com.iit.internship_manager.domain.models.Affectation;
import com.iit.internship_manager.domain.models.Enseignant;
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

    // 1. Year-Aware availability check for groups/students
    boolean existsByGroupeMembresIdInAndAnneeUniversitaire(List<Long> memberIds, AcademicYear year);

    boolean existsByGroupeMembresIdAndAnneeUniversitaire(Long studentId, AcademicYear year);

    // 2. Fetch for Teacher View (Filtered by Year)
    @Query(value = "SELECT a FROM Affectation a " +
            "JOIN FETCH a.groupe g " +
            "JOIN FETCH g.membres " +
            "JOIN FETCH a.encadrant " +
            "JOIN FETCH a.sujet " +
            "WHERE a.encadrant.id = :teacherId AND a.anneeUniversitaire = :year", countQuery = "SELECT COUNT(a) FROM Affectation a WHERE a.encadrant.id = :teacherId AND a.anneeUniversitaire = :year")
    Page<Affectation> findByEncadrantIdAndAnneeUniversitaire(@Param("teacherId") Long teacherId,
            @Param("year") AcademicYear year, Pageable pageable);

    // 3. Fetch for Student (Group-Aware & Year-Aware)
    @Query("SELECT a FROM Affectation a JOIN a.groupe g JOIN g.membres m WHERE m.id = :studentId AND a.anneeUniversitaire = :year")
    List<Affectation> findByGroupeMembresIdAndAnneeUniversitaire(@Param("studentId") Long studentId,
            @Param("year") AcademicYear year);

    // 4. Responsable PFE View: Department-wide filtering by Year
    @Query("SELECT a FROM Affectation a WHERE a.encadrant.department = :dept AND a.anneeUniversitaire = :year")
    Page<Affectation> findByDepartmentAndAnneeUniversitaire(@Param("dept") DepartmentType dept,
            @Param("year") AcademicYear year, Pageable pageable);

    // 5. Dynamic Quota counting for Teacher Workload
    long countByEncadrantAndAnneeUniversitaire(Enseignant teacher, AcademicYear year);

    // 6. Statistics / Global Counts
    long countByAnneeUniversitaire(AcademicYear year);

    @Query("SELECT COUNT(a) FROM Affectation a WHERE a.encadrant.department = :dept AND a.anneeUniversitaire = :year")
    long countByDepartmentAndAnneeUniversitaire(@Param("dept") DepartmentType dept, @Param("year") AcademicYear year);

    // --- Legacy / Specific Lookups ---

    Optional<Affectation> findByGroupeId(Long groupeId);

    @Query("SELECT a FROM Affectation a JOIN FETCH a.groupe g JOIN FETCH g.membres WHERE a.encadrant.id = :teacherId")
    List<Affectation> findByEncadrantId(@Param("teacherId") Long teacherId);
}