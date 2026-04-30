package com.iit.internship_manager.services.registration;

import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.exceptions.DomainException;
import com.iit.internship_manager.domain.models.Enseignant;
import com.iit.internship_manager.infrastucture.mappers.EnseignantMapper;
import com.iit.internship_manager.repositories.EnseignantRepository;
import com.iit.internship_manager.web.dtos.UserResponseDTO;
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

    @Override
    public UserType getSupportedType() {
        return UserType.TEACHER;
    }

    @Override
    @Transactional
    public UserResponseDTO register(TeacherRegisterRequest req) {
        // 1. Validation
        if (enseignantRepository.existsByEmail(req.getEmail())) {
            throw new DomainException(ErrorCode.CONFLICT, "Email already used.");
        }

        // 2. Mapping & Security
        // Note: Department is mapped automatically here by
        // enseignantMapper.toEntity(req)
        Enseignant en = enseignantMapper.toEntity(req);

        en.setPassword(passwordEncoder.encode(req.getPassword()));
        en.setRole(Role.ENSEIGNANT);

        // These fields are already set to 0 and true in the entity defaults or Mapper,
        // but explicit setting here is fine for clarity.
        en.setEncadrementsActuels(0);
        en.setActive(true);

        // 3. Persist
        enseignantRepository.save(en);


        // 5. Return AuthResponse
        return UserResponseDTO.fromEntity(en);
    }
}