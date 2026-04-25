package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.services.AuthenticationService;
import com.iit.internship_manager.services.registration.RegistrationStrategy;
import com.iit.internship_manager.services.registration.RegistrationStrategyFactory;
import com.iit.internship_manager.web.dtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth") // matches your SecurityConfig permitAll path
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final RegistrationStrategyFactory factory;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegistrationStrategy<RegisterRequest> strategy = factory.resolve(request.getUserType());
        return ApiResponse.success("User registered successfully", strategy.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }
}