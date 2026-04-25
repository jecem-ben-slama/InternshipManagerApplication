// EnseignantRegistrationStrategy.java
package com.iit.internship_manager.services.registration;

import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.exceptions.EmailAlreadyUsedException;
import com.iit.internship_manager.domain.models.Enseignant;
import com.iit.internship_manager.repositories.EnseignantRepository;
import com.iit.internship_manager.web.dtos.*;
import com.iit.internship_manager.web.dtos.registration.EnseignantRegisterRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnseignantRegistrationStrategy
        implements RegistrationStrategy<EnseignantRegisterRequest> {

    private final EnseignantRepository enseignantRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserType getSupportedType() {
        return UserType.TEACHER;
    }

    @Override
    @Transactional
    public AuthResponse register(EnseignantRegisterRequest req) {
        if (enseignantRepository.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyUsedException(req.getEmail());
        }

        Enseignant en = new Enseignant();
        en.setEmail(req.getEmail());
        en.setPassword(passwordEncoder.encode(req.getPassword()));
        en.setNom(req.getNom());
        en.setPrenom(req.getPrenom());
        en.setRole(Role.ENSEIGNANT); // always ENSEIGNANT — never changes
        en.setResponsablePFE(req.isResponsablePFE()); // just a flag, not a role change
        en.setSpecialites(req.getSpecialites());

        enseignantRepository.save(en);
        return AuthResponse.of(en);
    }
}