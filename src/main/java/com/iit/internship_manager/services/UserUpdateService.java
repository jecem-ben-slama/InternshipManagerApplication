package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
import com.iit.internship_manager.domain.exceptions.UnauthorizedActionException;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.services.interfaces.ISecurityContext; // Inject this
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
    private final List<UserUpdateStrategy> updateStrategies;
    private final ISecurityContext securityContext; // New dependency

    @Transactional
    public UserResponseDTO update(Long id, UpdateRequest dto) {
        // 1. Fetch the target user
        Utilisateur targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        // 2. SECURITY CHECK: Who is allowed to update this?
        validateUpdatePermission(targetUser);

        // 3. Update common fields
        targetUser.setNom(dto.getNom());
        targetUser.setPrenom(dto.getPrenom());
        targetUser.setEmail(dto.getEmail());

        // Only an Admin should be able to change a Role!
        if (securityContext.hasRole("ADMIN_IT")) {
            targetUser.setRole(dto.getRole());
        }

        // 4. Strategy Execution
        updateStrategies.stream()
                .filter(strategy -> strategy.supports(targetUser, dto))
                .findFirst()
                .orElseThrow(
                        () -> new UnauthorizedActionException("Type d'utilisateur non supporté pour la mise à jour"))
                .update(targetUser, dto);

        return UserResponseDTO.fromEntity(userRepository.save(targetUser));
    }

    /**
     * Internal logic to decide if the current user can modify the target user.
     */
    private void validateUpdatePermission(Utilisateur targetUser) {
        Long currentUserId = securityContext.getCurrentUserId();

        // Rule 1: You can always update yourself
        if (targetUser.getId().equals(currentUserId)) {
            return;
        }

        // Rule 2: Only Admin_IT can update other people
        if (!securityContext.hasRole("ADMIN_IT")) {
            throw new UnauthorizedActionException("Vous n'avez pas la permission de modifier ce profil.");
        }
    }
}