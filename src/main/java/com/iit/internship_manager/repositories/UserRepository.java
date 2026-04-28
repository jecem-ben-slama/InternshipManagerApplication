// UtilisateurRepository.java
package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.enums.DepartmentType; // Add this import
import com.iit.internship_manager.domain.models.Enseignant;
import com.iit.internship_manager.domain.models.Etudiant;
import com.iit.internship_manager.domain.models.Utilisateur;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<Utilisateur, Long> {
    boolean existsByEmail(String email);

    Optional<Utilisateur> findByEmailAndActiveTrue(String email);

    @Query("SELECT e FROM Enseignant e WHERE e.id = :id")
    Optional<Enseignant> findEnseignantById(@Param("id") Long id);

    Page<Utilisateur> findAllByActiveTrue(Pageable pageable);

    Optional<Utilisateur> findByEmail(String email);

    // --- Department Specific Queries ---

    // Finds any active user (Student, Teacher, Admin) by department
    Page<Utilisateur> findAllByDepartmentAndActiveTrue(Pageable pageable, DepartmentType department);

    // Specifically find teachers in a department for subject assignment
    @Query("SELECT e FROM Enseignant e WHERE e.department = :dept AND e.active = true")
    Page<Enseignant> findEnseignantsByDepartment(@Param("dept") DepartmentType dept, Pageable pageable);

    // --- Specific Logic Queries ---

    @Query("SELECT e FROM Etudiant e WHERE e.id NOT IN " +
            "(SELECT m.id FROM Affectation a JOIN a.groupe g JOIN g.membres m)")
    List<Etudiant> findStudentsWithoutAffectation();

    @Query("SELECT e FROM Enseignant e")
    List<Enseignant> findAllTeachers();

    // 1. Fix for getUnassignedStudents: Finds Etudiants not in any Affectation
    // group
    @Query("SELECT u FROM Etudiant u WHERE NOT EXISTS " +
            "(SELECT a FROM Affectation a JOIN a.groupe g JOIN g.membres m WHERE m.id = u.id)")
    Page<Etudiant> findStudentsWithoutAffectation(Pageable pageable);

    // 2. Fix for getTeachersWorkload: Specifically fetches Enseignant types with
    // pagination
    @Query("SELECT u FROM Enseignant u")
    Page<Enseignant> findAllEnseignants(Pageable pageable);
    
    // Add these to UserRepository.java
    // Finds unassigned students filtered by department
    @Query("SELECT u FROM Etudiant u WHERE u.department = :dept AND NOT EXISTS " +
            "(SELECT a FROM Affectation a JOIN a.groupe g JOIN g.membres m WHERE m.id = u.id)")
    Page<Etudiant> findStudentsWithoutAffectationByDepartment(@Param("dept") DepartmentType dept, Pageable pageable);

    // Check if a specific department has any active teachers (useful for
    // validation)
    boolean existsByDepartmentAndActiveTrue(DepartmentType department);
}