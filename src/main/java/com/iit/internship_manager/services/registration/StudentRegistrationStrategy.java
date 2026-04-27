package com.iit.internship_manager.services.registration;

import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.exceptions.EmailAlreadyUsedException;
import com.iit.internship_manager.domain.exceptions.MatriculeAlreadyUsedException;
import com.iit.internship_manager.domain.models.Etudiant;
import com.iit.internship_manager.infrastucture.security.JwtUtils; // New Injection
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
    private final JwtUtils jwtUtils; // Added to generate token upon registration

    @Override
    public UserType getSupportedType() {
        return UserType.STUDENT;
    }

    @Override
    @Transactional
    public AuthResponse register(StudentRegisterRequest req) {
        // 1. Validations
        if (etudiantRepository.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyUsedException(req.getEmail());
        }
        if (etudiantRepository.existsByMatricule(req.getMatricule())) {
            throw new MatriculeAlreadyUsedException(req.getMatricule());
        }

        // 2. Mapping
        Etudiant e = new Etudiant();
        e.setEmail(req.getEmail());
        e.setPassword(passwordEncoder.encode(req.getPassword()));
        e.setNom(req.getNom());
        e.setPrenom(req.getPrenom());
        e.setRole(Role.ETUDIANT);
        e.setMatricule(req.getMatricule());
        e.setFiliere(req.getFiliere());
        e.setAnneeEtude(req.getAnneeEtude());
        e.setActive(true); // Ensure new students are active by default

        // 3. Persist
        etudiantRepository.save(e);

        // 4. Automatic Login: Generate token for the new student
        String token = jwtUtils.generateToken(e);

        // 5. Return updated AuthResponse (Fixes the undefined method error)
        return AuthResponse.of(e, token);
    }
}