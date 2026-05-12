package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.enums.DepartmentType;
import com.iit.internship_manager.domain.enums.ErrorCode;
import com.iit.internship_manager.domain.exceptions.DomainException;
import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
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
            throw new DomainException(ErrorCode.FORBIDDEN, "Accès refusé : cet utilisateur appartient à un autre département.");
        }

        return UserResponseDTO.fromEntity(targetUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (securityContext.hasRole("ADMIN_IT")) {
            return userRepository.findAll(pageable).map(UserResponseDTO::fromEntity);
        }

        DepartmentType userDept = securityContext.getCurrentUser().getDepartment();
        return userRepository.findAllByDepartment(pageable, userDept)
                .map(UserResponseDTO::fromEntity);
    }

 @Override
@Transactional(readOnly = true)
public Page<UserResponseDTO> getTeachersByMyDepartment(int page, int size) {
    // Automatically extract the current authenticated user
    Utilisateur currentUser = securityContext.getCurrentUser();
    Pageable pageable = PageRequest.of(page, size);

    // ADMIN Logic: Admins aren't bound by departments, so they get everyone.
    if (securityContext.hasRole("ADMIN_IT")) {
        return userRepository.findAvailableTeachers(pageable)
                .map(UserResponseDTO::fromEntity);
    }

    // Student/Teacher Logic: Force the department from the user's own profile
    return userRepository.findAvailableTeachersByDepartment(currentUser.getDepartment(), pageable)
            .map(UserResponseDTO::fromEntity);
}

@Override
@Transactional(readOnly = true)
public Page<UserResponseDTO> getStudentsByMyDepartment(int page, int size) {
    Utilisateur currentUser = securityContext.getCurrentUser();
    Pageable pageable = PageRequest.of(page, size);

    if (securityContext.hasRole("ADMIN_IT")) {
        return userRepository.findAvailableStudents(pageable)
                .map(UserResponseDTO::fromEntity);
    }

    // Automatically restricts student search to peers in the same department
    return userRepository.findAvailableStudentsByDepartment(currentUser.getDepartment(), pageable)
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
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAllInactive(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (securityContext.hasRole("ADMIN_IT")) {
            return userRepository.findAllByActiveFalse(pageable).map(UserResponseDTO::fromEntity);
        }

        DepartmentType userDept = securityContext.getCurrentUser().getDepartment();
        return userRepository.findAllByDepartmentAndActiveFalse(pageable, userDept)
                .map(UserResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // Prevent self-deactivation
        if (id.equals(securityContext.getCurrentUserId())) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Vous ne pouvez pas désactiver votre propre compte.");
        }

        Utilisateur user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));

        // Security check for Responsables: cannot delete users from other depts
        if (!securityContext.hasRole("ADMIN_IT") &&
                !user.getDepartment().equals(securityContext.getCurrentUser().getDepartment())) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Action interdite pour ce département.");
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
            throw new DomainException(ErrorCode.FORBIDDEN, "Vous n'avez pas accès aux données de ce département.");
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
