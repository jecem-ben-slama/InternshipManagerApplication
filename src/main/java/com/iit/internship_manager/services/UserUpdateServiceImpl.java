package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.enums.ErrorCode;
import com.iit.internship_manager.domain.exceptions.DomainException;
import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.repositories.UserRepository;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserUpdateServiceImpl implements IUserUpdateService {

    private final UserRepository userRepository;
    private final List<UserUpdateStrategy> updateStrategies;
    private final ISecurityContext securityContext;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDTO update(Long id, UpdateRequest dto) {
        // 1. Fetch target
        Utilisateur targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        // 2. Validate Permission (Self or Admin)
        validateUpdatePermission(targetUser);

        // 3. Map Common Fields (Base Utilisateur fields)
        targetUser.setNom(dto.getNom());
        targetUser.setPrenom(dto.getPrenom());
        targetUser.setEmail(dto.getEmail());

        // NEW: Centralized mapping for the department
        targetUser.setDepartment(dto.getDepartment());

        if (StringUtils.hasText(dto.getPassword())) {
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
