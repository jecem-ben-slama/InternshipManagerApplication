package com.iit.internship_manager.services.registration;

import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.models.AdminIT;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.infrastucture.security.JwtUtils; // New Injection
import com.iit.internship_manager.web.dtos.*;
import com.iit.internship_manager.web.dtos.registration.AdminRegisterRequest;
import com.iit.internship_manager.domain.exceptions.EmailAlreadyUsedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminITRegistrationStrategy
        implements RegistrationStrategy<AdminRegisterRequest> {

    private final UserRepository userRepository; // Renamed for consistency
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils; // Needed to return a token after registration

    @Override
    public UserType getSupportedType() {
        return UserType.ADMIN_IT;
    }

    @Override
    @Transactional
    public AuthResponse register(AdminRegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyUsedException(req.getEmail());
        }

        AdminIT admin = new AdminIT();
        admin.setEmail(req.getEmail());
        admin.setPassword(passwordEncoder.encode(req.getPassword()));
        admin.setNom(req.getNom());
        admin.setPrenom(req.getPrenom());
        admin.setRole(Role.ADMIN_IT);
        admin.setActive(true); // Explicitly set active status

        userRepository.save(admin);

        // Generate token so the admin is logged in immediately
        String token = jwtUtils.generateToken(admin);

        // Use the new AuthResponse.of(user, token) format
        return AuthResponse.of(admin, token);
    }
}