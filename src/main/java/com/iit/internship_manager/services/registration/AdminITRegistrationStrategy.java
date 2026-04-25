// AdminITRegistrationStrategy.java
package com.iit.internship_manager.services.registration;

import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.models.AdminIT;
import com.iit.internship_manager.repositories.UtilisateurRepository;
import com.iit.internship_manager.web.dtos.*;
import com.iit.internship_manager.domain.exceptions.EmailAlreadyUsedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminITRegistrationStrategy
        implements RegistrationStrategy<AdminRegisterRequest> {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserType getSupportedType() {
        return UserType.ADMIN_IT;
    }

    @Override
    @Transactional
    public AuthResponse register(AdminRegisterRequest req) {
        if (utilisateurRepository.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyUsedException(req.getEmail());
        }

        AdminIT admin = new AdminIT();
        admin.setEmail(req.getEmail());
        admin.setPassword(passwordEncoder.encode(req.getPassword()));
        admin.setNom(req.getNom());
        admin.setPrenom(req.getPrenom());
        admin.setRole(Role.ADMIN_IT);

        utilisateurRepository.save(admin);
        return AuthResponse.of(admin);
    }
}