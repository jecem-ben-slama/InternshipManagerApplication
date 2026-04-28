// EnseignantRepository.java
package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.enums.DepartmentType;
import com.iit.internship_manager.domain.models.Enseignant;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnseignantRepository extends JpaRepository<Enseignant, Long> {
    boolean existsByEmail(String email);
    Optional<Enseignant> findByEmail(String email);
    Page<Enseignant> findByDepartmentAndActiveTrue(DepartmentType department, Pageable pageable);

}