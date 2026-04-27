package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.services.interfaces.IAuthenticationService;
import com.iit.internship_manager.web.dtos.*;
import com.iit.internship_manager.web.dtos.registration.RegisterRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final IAuthenticationService authenticationService;

   //* Register

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN_IT')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authenticationService.register(request);
        return ApiResponse.success("Utilisateur créé avec succès", response);
    }

    // * Login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Bienvenue, " + response.getUser().getPrenom(), response));
    }
}