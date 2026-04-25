package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.web.dtos.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Finds a single user by their ID and maps them to a DTO.
     * Uses readOnly = true for better performance.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        return userRepository.findById(id)
                .map(UserResponseDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + id, id));
    }

    /**
     * Returns a list of all users who are currently active (active = true).
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAllActive() {
        return userRepository.findAllByActiveTrue().stream()
                .map(UserResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Soft Delete: Instead of removing the user from the DB, we flip the active
     * flag.
     * This prevents data loss while blocking user access.
     */
    @Transactional
    public void delete(Long id) {
        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + id, id));

        user.setActive(false);
        userRepository.save(user);
    }

    /**
     * Reactivates a soft-deleted account.
     */
    @Transactional
    public void reactivate(Long id) {
        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id: " + id, id));

        user.setActive(true);
        userRepository.save(user);
    }

    /**
     * Helper to check if an email is already in use by anyone.
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}