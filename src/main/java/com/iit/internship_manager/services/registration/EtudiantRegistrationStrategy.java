// EtudiantRegistrationStrategy.java
package com.iit.internship_manager.services.registration;

import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.exceptions.EmailAlreadyUsedException;
import com.iit.internship_manager.domain.exceptions.MatriculeAlreadyUsedException;
import com.iit.internship_manager.domain.models.Etudiant;
import com.iit.internship_manager.repositories.EtudiantRepository;
import com.iit.internship_manager.web.dtos.*;
import com.iit.internship_manager.web.dtos.registration.EtudiantRegisterRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EtudiantRegistrationStrategy
        implements RegistrationStrategy<EtudiantRegisterRequest> {

    private final EtudiantRepository etudiantRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserType getSupportedType() {
        return UserType.STUDENT;
    }

    @Override
    @Transactional
    public AuthResponse register(EtudiantRegisterRequest req) {
        if (etudiantRepository.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyUsedException(req.getEmail());
        }
        if (etudiantRepository.existsByMatricule(req.getMatricule())) {
            throw new MatriculeAlreadyUsedException(req.getMatricule());
        }

        Etudiant e = new Etudiant();
        e.setEmail(req.getEmail());
        e.setPassword(passwordEncoder.encode(req.getPassword()));
        e.setNom(req.getNom());
        e.setPrenom(req.getPrenom());
        e.setRole(Role.ETUDIANT);
        e.setMatricule(req.getMatricule());
        e.setFiliere(req.getFiliere());
        e.setAnneeEtude(req.getAnneeEtude());

        etudiantRepository.save(e);
        return AuthResponse.of(e);
    }
}