package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.enums.DepartmentType;
import com.iit.internship_manager.domain.models.AcademicYear;
import com.iit.internship_manager.domain.models.Enseignant;
import com.iit.internship_manager.domain.models.Etudiant;
import com.iit.internship_manager.domain.models.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Utilisateur, Long> {

        // --- Basic Authentication & Exist Checks ---

        boolean existsByEmail(String email);

        Optional<Utilisateur> findByEmail(String email);

        Optional<Utilisateur> findByEmailAndActiveTrue(String email);

        boolean existsByDepartmentAndActiveTrue(DepartmentType department);

        // --- Casting & Single Table ID Queries ---

        @Query("SELECT e FROM Enseignant e WHERE e.id = :id")
        Optional<Enseignant> findEnseignantById(@Param("id") Long id);

        // --- General Listing (All Roles) ---

        Page<Utilisateur> findAllByActiveTrue(Pageable pageable);

        Page<Utilisateur> findAllByDepartmentAndActiveTrue(Pageable pageable, DepartmentType department);

        // --- Teacher Specific Queries (Admin/Global) ---

        @Query("SELECT u FROM Enseignant u WHERE u.active = true")
        Page<Enseignant> findAllEnseignants(Pageable pageable);

        @Query("SELECT e FROM Enseignant e")
        List<Enseignant> findAllTeachers();

        // --- Teacher Specific Queries (Department Scoped) ---

        @Query("SELECT e FROM Enseignant e WHERE e.department = :dept AND e.active = true")
        Page<Enseignant> findAllEnseignantsByDepartment(@Param("dept") DepartmentType dept, Pageable pageable);

        // --- Student Specific Queries (Admin/Global) ---

        @Query("SELECT u FROM Etudiant u WHERE u.active = true")
        Page<Etudiant> findAllEtudiants(Pageable pageable);

        // --- Student Specific Queries (Department Scoped) ---

        @Query("SELECT u FROM Etudiant u WHERE u.department = :dept AND u.active = true")
        Page<Etudiant> findAllEtudiantsByDepartment(@Param("dept") DepartmentType dept, Pageable pageable);

        // --- Workload & Unassigned Logic (Essential for AffectationService) ---

        /**
         * Finds students who are NOT part of any group that has been officially
         * assigned a project.
         */
        @Query("SELECT u FROM Etudiant u WHERE u.active = true AND NOT EXISTS " +
                        "(SELECT a FROM Affectation a JOIN a.groupe g JOIN g.membres m WHERE m.id = u.id)")
        Page<Etudiant> findStudentsWithoutAffectation(Pageable pageable);

        /**
         * Finds unassigned students specifically within a department (for Responsable
         * PFE view).
         */
        @Query("SELECT u FROM Etudiant u WHERE u.department = :dept AND u.active = true AND NOT EXISTS " +
                        "(SELECT a FROM Affectation a WHERE a.anneeUniversitaire = :year " +
                        "AND EXISTS (SELECT m FROM a.groupe.membres m WHERE m.id = u.id))")
        Page<Etudiant> findStudentsWithoutAffectationByDepartment(
                        @Param("dept") DepartmentType dept,
                        @Param("year") String year,
                        Pageable pageable);

        @Query("SELECT e FROM Etudiant e WHERE e.id NOT IN " +
                        "(SELECT m.id FROM Affectation a JOIN a.groupe g JOIN g.membres m)")
        List<Etudiant> findStudentsWithoutAffectationList();

        @Query("SELECT e FROM Etudiant e WHERE e.department = :dept " +
                        "AND NOT EXISTS ( " +
                        "  SELECT a FROM Affectation a JOIN a.groupe g JOIN g.membres m " +
                        "  WHERE m.id = e.id AND a.anneeUniversitaire = :year " +
                        ")")
        Page<Etudiant> findStudentsWithoutAffectationByDepartmentAndYear(
                        @Param("dept") DepartmentType dept,
                        @Param("year") AcademicYear year,
                        Pageable pageable);

}