// UtilisateurRepository.java
package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.models.Utilisateur;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    boolean existsByEmail(String email);
    
    Optional<Utilisateur> findByEmail(String email);

}