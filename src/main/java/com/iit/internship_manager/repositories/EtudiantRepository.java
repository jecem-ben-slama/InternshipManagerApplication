// EtudiantRepository.java
package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.models.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    boolean existsByEmail(String email);

    boolean existsByMatricule(String matricule);
}