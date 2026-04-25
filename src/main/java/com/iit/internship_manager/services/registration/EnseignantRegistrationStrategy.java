package com.iit.internship_manager.services.registration;

import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.exceptions.EmailAlreadyUsedException;
import com.iit.internship_manager.domain.models.Enseignant;
import com.iit.internship_manager.infrastucture.mappers.EnseignantMapperImpl;
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
    private final EnseignantMapperImpl enseignantMapper; // Injecting your new mapper

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
        Enseignant en = enseignantMapper.toEntity(req);
        en.setPassword(passwordEncoder.encode(req.getPassword()));
        en.setRole(Role.ENSEIGNANT);
        en.setEncadrementsActuels(0);
        enseignantRepository.save(en);
        return AuthResponse.of(en);
    }
}