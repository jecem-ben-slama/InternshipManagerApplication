package com.iit.internship_manager.services ;

import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.domain.exceptions.AccountDeactivatedException;
import com.iit.internship_manager.domain.exceptions.InvalidCredentialsException;
import com.iit.internship_manager.infrastucture.security.JwtUtils;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.web.dtos.LoginRequest;
import com.iit.internship_manager.web.dtos.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public ApiResponse<Map<String, Object>> login(LoginRequest req) {
        // 1. Find user by email
        Utilisateur user = utilisateurRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException());

        // 2. CHECK SOFT DELETE STATUS
        if (!user.isActive()) {
            throw new AccountDeactivatedException("Ce compte est désactivé. Veuillez contacter l'administrateur.");
        }

        // 3. Check password
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // 4. Generate JWT
        String token = jwtUtils.generateToken(user);

        // 5. Build response
        Map<String, Object> data = Map.of(
                "token", token,
                "id", user.getId(),
                "email", user.getEmail(),
                "nom", user.getNom(),
                "prenom", user.getPrenom(),
                "role", user.getRole().name());

        return ApiResponse.success("Connexion réussie", data);
    }
}