package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.enums.ErrorCode;
import com.iit.internship_manager.domain.exceptions.DomainException;
import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.services.interfaces.IFileStorageService;
import com.iit.internship_manager.services.interfaces.ISecurityContext;
import com.iit.internship_manager.services.interfaces.IUserUpdateService;
import com.iit.internship_manager.services.updateUser.UserUpdateStrategy;
import com.iit.internship_manager.web.dtos.UserResponseDTO;
import com.iit.internship_manager.web.dtos.updateUser.UpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserUpdateServiceImpl implements IUserUpdateService {

    private final UserRepository userRepository;
    private final List<UserUpdateStrategy> updateStrategies;
    private final ISecurityContext securityContext;
    private final PasswordEncoder passwordEncoder;
    private final IFileStorageService fileStorageService;

    @Override
    @Transactional
    public UserResponseDTO update(Long id, UpdateRequest dto) {
        // 1. Fetch target
        Utilisateur targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        // 2. Validate Permission (Self or Admin)
        validateUpdatePermission(targetUser);

        boolean selfUpdate = targetUser.getId().equals(securityContext.getCurrentUserId());

        // 3. Map Common Fields (Base Utilisateur fields)
        targetUser.setNom(dto.getNom());
        targetUser.setPrenom(dto.getPrenom());
        targetUser.setEmail(dto.getEmail());

        // NEW: Centralized mapping for the department
        targetUser.setDepartment(dto.getDepartment());

        if (!selfUpdate && StringUtils.hasText(dto.getPassword())) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Le mot de passe ne peut etre modifie que par son proprietaire depuis son profil.");
        }

        if (selfUpdate && StringUtils.hasText(dto.getPassword())) {
            targetUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // Security Rule: Role changes are restricted to IT Admins
        if (securityContext.hasRole("ADMIN_IT") && dto.getRole() != null) {
            targetUser.setRole(dto.getRole());
        }

        // 4. Delegate to the correct Strategy (Student, Teacher, or Admin)
        updateStrategies.stream()
                .filter(strategy -> strategy.supports(targetUser, dto))
                .findFirst()
                .orElseThrow(() -> new DomainException(ErrorCode.FORBIDDEN, "Combinaison type/utilisateur non supportée"))
                .update(targetUser, dto);

        return UserResponseDTO.fromEntity(userRepository.save(targetUser));
    }

    @Override
    @Transactional
    public UserResponseDTO uploadCurrentUserProfilePhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DomainException(ErrorCode.VALIDATION_FAILED, "Veuillez choisir une image a televerser.");
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.startsWith("image/")) {
            throw new DomainException(ErrorCode.VALIDATION_FAILED, "Le fichier choisi doit etre une image.");
        }

        long maxSize = 5L * 1024L * 1024L;
        if (file.getSize() > maxSize) {
            throw new DomainException(ErrorCode.VALIDATION_FAILED, "La photo de profil ne doit pas depasser 5 Mo.");
        }

        Utilisateur currentUser = securityContext.getCurrentUser();

        if (StringUtils.hasText(currentUser.getProfilePhoto())) {
            fileStorageService.delete(currentUser.getProfilePhoto());
        }

        String storedName = fileStorageService.store(file);
        currentUser.setProfilePhoto(storedName);
        currentUser.setProfilePhotoContentType(contentType);

        return UserResponseDTO.fromEntity(userRepository.save(currentUser));
    }

    private void validateUpdatePermission(Utilisateur targetUser) {
        Long currentUserId = securityContext.getCurrentUserId();

        // Allow if updating own profile
        if (targetUser.getId().equals(currentUserId)) {
            return;
        }

        // Allow if current user is Admin
        if (!securityContext.hasRole("ADMIN_IT")) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Accès refusé : vous ne pouvez modifier que votre propre profil.");
        }
    }
}
