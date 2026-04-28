package com.iit.internship_manager.services;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.domain.exceptions.AccountDeactivatedException;
import com.iit.internship_manager.domain.exceptions.InvalidCredentialsException;
import com.iit.internship_manager.infrastucture.security.JwtUtils;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.services.interfaces.IAuthenticationService;
import com.iit.internship_manager.services.registration.RegistrationStrategy;
import com.iit.internship_manager.services.registration.RegistrationStrategyFactory;
import com.iit.internship_manager.web.dtos.LoginRequest;
import com.iit.internship_manager.web.dtos.AuthResponse;
import com.iit.internship_manager.web.dtos.UserResponseDTO;
import com.iit.internship_manager.web.dtos.registration.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements IAuthenticationService {

    private final RegistrationStrategyFactory registrationFactory;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        // 1. Fetch user
        Utilisateur user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            throw new AccountDeactivatedException(
                    "Ce compte est désactivé. Veuillez contacter l'administration.");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        String token = jwtUtils.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .user(UserResponseDTO.fromEntity(user))
                .build();
    }

    // * Register a new user (Admin, Student, Teacher)
    @Override
    @Transactional
    public UserResponseDTO register(RegisterRequest request) {
        RegistrationStrategy<RegisterRequest> strategy = registrationFactory.resolve(request.getUserType());
        return strategy.register(request);
    }
}