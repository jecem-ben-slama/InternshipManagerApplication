// UtilisateurRepository.java
package com.iit.internship_manager.repositories;
import com.iit.internship_manager.domain.models.Utilisateur;
import java.util.Optional;
import org.springframework.data.domain.Page; // Add this import
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Utilisateur, Long> {
    boolean existsByEmail(String email);
    
    Optional<Utilisateur> findByEmailAndActiveTrue(String email);
    
    Page<Utilisateur> findAllByActiveTrue(Pageable pageable);    
    Optional<Utilisateur> findByEmail(String email);

}