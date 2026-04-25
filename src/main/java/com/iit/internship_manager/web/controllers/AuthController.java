package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.infrastucture.security.AuthService;
import com.iit.internship_manager.web.dtos.ApiResponse;
import com.iit.internship_manager.web.dtos.AuthResponse;
import com.iit.internship_manager.web.dtos.LoginRequest;
import com.iit.internship_manager.web.dtos.RegisterRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN_IT')") // Only Admin IT can register new users
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }
}