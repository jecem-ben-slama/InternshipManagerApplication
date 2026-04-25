package com.iit.internship_manager.infrastucture.security;

import com.iit.internship_manager.domain.enums.Role;
import com.iit.internship_manager.domain.enums.SpecialiteType;
import com.iit.internship_manager.domain.models.AdminIT;
import com.iit.internship_manager.domain.models.Enseignant;
import com.iit.internship_manager.domain.models.Etudiant;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.domain.exceptions.UnauthorizedActionException;
import com.iit.internship_manager.repositories.UtilisateurRepository;
import com.iit.internship_manager.web.dtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final AuthenticationManager authenticationManager;
        private final UtilisateurRepository utilisateurRepository;
        private final JwtUtils jwtUtils;
        private final PasswordEncoder passwordEncoder;

        public AuthResponse login(LoginRequest request) {
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

                Utilisateur user = utilisateurRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new UnauthorizedActionException("Invalid credentials"));

                return createAuthResponse(user);
        }

        public AuthResponse register(RegisterRequest request) {
                if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
                        throw new RuntimeException("Email already in use");
                }

                Utilisateur user;

                // 1. Handle Polymorphic Instantiation
                if (request.getRole() == Role.ENSEIGNANT) {
                        Enseignant teacher = new Enseignant();
                        teacher.setResponsablePFE(request.isResponsablePFE());
                        // Fix: Explicitly typed HashSet to avoid "Cannot infer type arguments"
                        if (request.getSpecialites() != null) {
                                // Use SpecialiteType here as per your Enseignant.java definition
                                teacher.setSpecialites(new HashSet<SpecialiteType>(request.getSpecialites()));
                        }
                        user = teacher;
                } else if (request.getRole() == Role.ADMIN_IT) {
                        user = new AdminIT();
                } else {
                        // Default to Student
                        Etudiant student = new Etudiant();
                        student.setMatricule(request.getMatricule());
                        student.setFiliere(request.getFiliere());
                        student.setAnneeEtude(request.getAnneeEtude());
                        user = student;
                }

                // 2. Map Common Fields
                user.setEmail(request.getEmail());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setNom(request.getNom());
                user.setPrenom(request.getPrenom());
                // Set role, defaulting to ETUDIANT if null
                user.setRole(request.getRole() != null ? request.getRole() : Role.ETUDIANT);

                // 3. Persist and Return
                try {
                        Utilisateur savedUser = utilisateurRepository.save(user);
                        return createAuthResponse(savedUser);
                } catch (Exception e) {
                        // Log the detailed error to the console for easier debugging
                        System.err.println("CRITICAL REGISTRATION ERROR: " + e.getMessage());
                        throw e;
                }
        }

        private AuthResponse createAuthResponse(Utilisateur user) {
                String token = jwtUtils.generateToken(user);
                AuthResponse response = new AuthResponse();
                response.setToken(token);
                response.setId(user.getId());
                response.setEmail(user.getEmail());
                response.setRole(user.getRole().name());
                return response;
        }
}