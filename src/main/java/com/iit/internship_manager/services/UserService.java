package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
import com.iit.internship_manager.domain.exceptions.UnauthorizedActionException;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.services.interfaces.ISecurityContext;
import com.iit.internship_manager.web.dtos.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ISecurityContext securityContext; // New Injection

    /**
     * Gets the profile of the currently authenticated user.
     * Essential for the Flutter "Profile" tab.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUserProfile() {
        return UserResponseDTO.fromEntity(securityContext.getCurrentUser());
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        return userRepository.findById(id)
                .map(UserResponseDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAllActive(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAllByActiveTrue(pageable)
                .map(UserResponseDTO::fromEntity);
    }

    @Transactional
    public void delete(Long id) {
        // 1. Protection: A user shouldn't be able to deactivate themselves
        if (id.equals(securityContext.getCurrentUserId())) {
            throw new UnauthorizedActionException("Vous ne pouvez pas désactiver votre propre compte.");
        }

        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void reactivate(Long id) {
        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        user.setActive(true);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}