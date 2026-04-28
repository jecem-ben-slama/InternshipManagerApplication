package com.iit.internship_manager.services.interfaces;

import com.iit.internship_manager.web.dtos.LoginRequest;
import com.iit.internship_manager.web.dtos.UserResponseDTO;
import com.iit.internship_manager.web.dtos.AuthResponse;
import com.iit.internship_manager.web.dtos.registration.RegisterRequest;

public interface IAuthenticationService {
    AuthResponse login(LoginRequest request);

    UserResponseDTO register(RegisterRequest request);
}