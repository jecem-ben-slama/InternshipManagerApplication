package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.enums.ErrorCode;
import com.iit.internship_manager.domain.exceptions.DomainException;
import com.iit.internship_manager.domain.models.Enseignant;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.services.interfaces.ISecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityContextImpl implements ISecurityContext {

    private final UserRepository userRepository;

    @Override
    public Utilisateur getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Utilisateur introuvable dans la base de données."));
    }

    @Override
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    @Override
    public String getCurrentUserEmail() {
        // Standard check to ensure Authentication is not null
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Utilisateur non authentifié.");
        }
        return auth.getName();
    }

    @Override
    public boolean hasRole(String role) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return false;

        return auth.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    @Override
    public boolean isAdmin() {
        return hasRole("ADMIN_IT");
    }

    @Override
    public boolean isResponsablePFE() {
        Utilisateur user = getCurrentUser();

        // Only Enseignants can be Responsables based on your domain model
        if (user instanceof Enseignant teacher) {
            return teacher.isResponsablePFE();
        }

        return false;
    }
}
