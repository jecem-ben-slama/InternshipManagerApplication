package com.iit.internship_manager.services.registration;

import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.exceptions.DomainException;
import com.iit.internship_manager.domain.models.Etudiant;
import com.iit.internship_manager.repositories.EtudiantRepository;
import com.iit.internship_manager.web.dtos.*;
import com.iit.internship_manager.web.dtos.registration.StudentRegisterRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentRegistrationStrategy
        implements RegistrationStrategy<StudentRegisterRequest> {

    private final EtudiantRepository etudiantRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public UserType getSupportedType() {
        return UserType.STUDENT;
    }

    @Override
    @Transactional
    public UserResponseDTO register(StudentRegisterRequest req) {
        // 1. Validations
        if (etudiantRepository.existsByEmail(req.getEmail())) {
            throw new DomainException(ErrorCode.CONFLICT, "Email already used.");
        }
        if (etudiantRepository.existsByMatricule(req.getMatricule())) {
            throw new DomainException(ErrorCode.CONFLICT, "Matricule already used.");
        }

        // 2. Mapping
        Etudiant e = new Etudiant();
        e.setEmail(req.getEmail());
        e.setPassword(passwordEncoder.encode(req.getPassword()));
        e.setNom(req.getNom());
        e.setPrenom(req.getPrenom());

        // NEW: Centralized department field inherited from RegisterRequest
        e.setDepartment(req.getDepartment());

        e.setRole(Role.ETUDIANT);
        e.setMatricule(req.getMatricule());
        e.setFiliere(req.getFiliere());
        e.setAnneeEtude(req.getAnneeEtude());
        e.setActive(true);

        // 3. Persist
        etudiantRepository.save(e);

        // 4. Automatic Login

        // 5. Return Response
        return UserResponseDTO.fromEntity(e);
    }
}