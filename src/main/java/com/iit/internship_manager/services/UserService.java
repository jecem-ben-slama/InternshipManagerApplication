package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.web.dtos.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // * getByID
    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        return userRepository.findById(id)
                .map(UserResponseDTO::fromEntity)
                // Updated to match your ResourceNotFoundException(String resource, Long id)
                // constructor
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
    }

    // * getAll
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAllActive(int pageNumber) {
        // Create a page request with 10 items per page
        Pageable pageable = PageRequest.of(pageNumber, 10);

        // The repository returns a Page<Utilisateur>
        Page<Utilisateur> userPage = userRepository.findAllByActiveTrue(pageable);

        // Map the Page of entities to a Page of DTOs
        return userPage.map(UserResponseDTO::fromEntity);
    }

    // * deactivate
    @Transactional
    public void delete(Long id) {
        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        user.setActive(false);
        userRepository.save(user);
    }

    // * reactivate
    @Transactional
    public void reactivate(Long id) {
        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        user.setActive(true);
        userRepository.save(user);
    }

    // * check if email exists
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}