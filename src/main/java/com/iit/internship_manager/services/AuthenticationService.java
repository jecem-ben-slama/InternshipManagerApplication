package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.domain.exceptions.AccountDeactivatedException;
import com.iit.internship_manager.domain.exceptions.InvalidCredentialsException;
import com.iit.internship_manager.infrastucture.security.JwtUtils;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.services.registration.RegistrationStrategy;
import com.iit.internship_manager.services.registration.RegistrationStrategyFactory;
import com.iit.internship_manager.web.dtos.LoginRequest;
import com.iit.internship_manager.web.dtos.ApiResponse;
import com.iit.internship_manager.web.dtos.AuthResponse;
import com.iit.internship_manager.web.dtos.UserResponseDTO; // Use your DTO!
import com.iit.internship_manager.web.dtos.registration.RegisterRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final RegistrationStrategyFactory registrationFactory;
    private final UserRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Transactional(readOnly = true)
    public ApiResponse<Map<String, Object>> login(LoginRequest req) {
        // 1. Fetch user
        Utilisateur user = utilisateurRepository.findByEmail(req.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        // 2. Check Soft Delete
        if (!user.isActive()) {
            throw new AccountDeactivatedException(
                    "Ce compte est désactivé. Veuillez contacter l'administration de l'IIT.");
        }

        // 3. Verify Password
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // 4. Generate Token
        String token = jwtUtils.generateToken(user);

        // 5. Build Unified Response
        // Instead of a manual Map, we use the UserResponseDTO to ensure the Flutter app
        // receives the exact same user object format as the /me endpoint.
        Map<String, Object> data = Map.of(
                "token", token,
                "user", UserResponseDTO.fromEntity(user));

        return ApiResponse.success("Bienvenue, " + user.getPrenom(), data);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Resolve the strategy and execute
        RegistrationStrategy<RegisterRequest> strategy = registrationFactory.resolve(request.getUserType());
        return strategy.register(request);
    }
}