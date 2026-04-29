package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.enums.DepartmentType;
import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
import com.iit.internship_manager.domain.exceptions.UnauthorizedActionException;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.services.interfaces.ISecurityContext;
import com.iit.internship_manager.services.interfaces.IUserService;
import com.iit.internship_manager.web.dtos.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final ISecurityContext securityContext;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUserProfile() {
        return UserResponseDTO.fromEntity(securityContext.getCurrentUser());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        Utilisateur targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        Utilisateur currentUser = securityContext.getCurrentUser();

        // Security Guard: Only Admins can view cross-department profiles.
        // Others are restricted to their own department.
        if (!securityContext.hasRole("ADMIN_IT") &&
                !targetUser.getDepartment().equals(currentUser.getDepartment())) {
            throw new UnauthorizedActionException("Accès refusé : cet utilisateur appartient à un autre département.");
        }

        return UserResponseDTO.fromEntity(targetUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getTeachersByMyDepartment(int page, int size, DepartmentType filterDept) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        // 1. ADMIN Logic: Global access with optional department filtering
        if (securityContext.hasRole("ADMIN_IT")) {
            if (filterDept != null) {
                return userRepository.findAllEnseignantsByDepartment(filterDept, pageable)
                        .map(UserResponseDTO::fromEntity);
            }
            return userRepository.findAllEnseignants(pageable).map(UserResponseDTO::fromEntity);
        }

        // 2. Standard/Responsable Logic: Forced to current user's department
        return userRepository.findAllEnseignantsByDepartment(currentUser.getDepartment(), pageable)
                .map(UserResponseDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getStudentsByMyDepartment(int page, int size, DepartmentType filterDept) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        // 1. ADMIN Logic: Global access with optional department filtering
        if (securityContext.hasRole("ADMIN_IT")) {
            if (filterDept != null) {
                return userRepository.findAllEtudiantsByDepartment(filterDept, pageable)
                        .map(UserResponseDTO::fromEntity);
            }
            return userRepository.findAllEtudiants(pageable).map(UserResponseDTO::fromEntity);
        }

        // 2. Standard/Responsable Logic: Forced to current user's department
        return userRepository.findAllEtudiantsByDepartment(currentUser.getDepartment(), pageable)
                .map(UserResponseDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAllActive(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Admins see everything, others see only their department's active users
        if (securityContext.hasRole("ADMIN_IT")) {
            return userRepository.findAllByActiveTrue(pageable).map(UserResponseDTO::fromEntity);
        }

        DepartmentType userDept = securityContext.getCurrentUser().getDepartment();
        return userRepository.findAllByDepartmentAndActiveTrue(pageable, userDept)
                .map(UserResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // Prevent self-deactivation
        if (id.equals(securityContext.getCurrentUserId())) {
            throw new UnauthorizedActionException("Vous ne pouvez pas désactiver votre propre compte.");
        }

        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        // Security check for Responsables: cannot delete users from other depts
        if (!securityContext.hasRole("ADMIN_IT") &&
                !user.getDepartment().equals(securityContext.getCurrentUser().getDepartment())) {
            throw new UnauthorizedActionException("Action interdite pour ce département.");
        }

        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void reactivate(Long id) {
        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAllByDepartment(DepartmentType department, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Verify access: If not admin, the requested department must match user
        // department
        if (!securityContext.hasRole("ADMIN_IT") &&
                !securityContext.getCurrentUser().getDepartment().equals(department)) {
            throw new UnauthorizedActionException("Vous n'avez pas accès aux données de ce département.");
        }

        return userRepository.findAllByDepartmentAndActiveTrue(pageable, department)
                .map(UserResponseDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}