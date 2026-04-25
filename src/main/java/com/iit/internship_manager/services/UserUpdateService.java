package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.services.updateUser.UserUpdateStrategy;
import com.iit.internship_manager.web.dtos.UserResponseDTO;
import com.iit.internship_manager.web.dtos.updateUser.UpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserUpdateService {

    private final UserRepository userRepository;
    // Spring automatically injects all classes that implement UserUpdateStrategy
    // into this list
    private final List<UserUpdateStrategy> updateStrategies;

    @Transactional
    public UserResponseDTO update(Long id, UpdateRequest dto) {
        // 1. Fetch the user from the database
        Utilisateur user = userRepository.findById(id)
                // Updated to use the clean constructor: resource name + id
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        // 2. Update common fields (shared by all users)
        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());

        // 3. The "Strategy Selector" logic
        // We look through our list of strategies to find the one that 'supports' this
        // user/dto
        updateStrategies.stream()
                .filter(strategy -> strategy.supports(user, dto))
                .findFirst()
                // You could also create a custom "StrategyNotFoundException" extending
                // DomainException here
                .orElseThrow(() -> new RuntimeException("Aucune stratégie trouvée pour ce type d'utilisateur"))
                .update(user, dto); // <--- Here, the specialist takes over!

        // 4. Save and return
        Utilisateur updatedUser = userRepository.save(user);
        return UserResponseDTO.fromEntity(updatedUser);
    }
}