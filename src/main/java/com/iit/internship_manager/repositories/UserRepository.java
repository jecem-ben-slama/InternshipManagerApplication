// UtilisateurRepository.java
package com.iit.internship_manager.repositories;
import com.iit.internship_manager.domain.models.Enseignant;
import com.iit.internship_manager.domain.models.Utilisateur;
import java.util.Optional;
import org.springframework.data.domain.Page; // Add this import
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<Utilisateur, Long> {
    boolean existsByEmail(String email);
    
    Optional<Utilisateur> findByEmailAndActiveTrue(String email);
@Query("SELECT e FROM Enseignant e WHERE e.id = :id")
    Optional<Enseignant> findEnseignantById(@Param("id") Long id);    Page<Utilisateur> findAllByActiveTrue(Pageable pageable);    
    Optional<Utilisateur> findByEmail(String email);

}