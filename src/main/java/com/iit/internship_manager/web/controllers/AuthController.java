package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.services.AuthenticationService;
import com.iit.internship_manager.web.dtos.*;
import com.iit.internship_manager.web.dtos.registration.RegisterRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth") // matches your SecurityConfig permitAll path
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN_IT')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Controller is now "Thin" and clean
        return ApiResponse.success(
                "Utilisateur créé avec succès",
                authenticationService.register(request));
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request);
    }
}