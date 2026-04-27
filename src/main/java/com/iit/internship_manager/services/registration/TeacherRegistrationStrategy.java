package com.iit.internship_manager.services.registration;

import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.exceptions.EmailAlreadyUsedException;
import com.iit.internship_manager.domain.models.Enseignant;
import com.iit.internship_manager.infrastucture.mappers.EnseignantMapper; // Use Interface
import com.iit.internship_manager.infrastucture.security.JwtUtils; // New Injection
import com.iit.internship_manager.repositories.EnseignantRepository;
import com.iit.internship_manager.web.dtos.AuthResponse;
import com.iit.internship_manager.web.dtos.registration.TeacherRegisterRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeacherRegistrationStrategy
        implements RegistrationStrategy<TeacherRegisterRequest> {

    private final EnseignantRepository enseignantRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnseignantMapper enseignantMapper;
    private final JwtUtils jwtUtils; // Needed to generate token upon registration

    @Override
    public UserType getSupportedType() {
        return UserType.TEACHER;
    }

    @Override
    @Transactional
    public AuthResponse register(TeacherRegisterRequest req) {
        // 1. Validation
        if (enseignantRepository.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyUsedException(req.getEmail());
        }

        // 2. Mapping & Security
        Enseignant en = enseignantMapper.toEntity(req);
        en.setPassword(passwordEncoder.encode(req.getPassword()));
        en.setRole(Role.ENSEIGNANT);
        en.setEncadrementsActuels(0);
        en.setActive(true); // Ensure new teachers are active by default

        // 3. Persist
        enseignantRepository.save(en);

        // 4. Generate Token (So they don't have to login immediately after signing up)
        String token = jwtUtils.generateToken(en);

        // 5. Return updated AuthResponse format
        return AuthResponse.of(en, token);
    }
}