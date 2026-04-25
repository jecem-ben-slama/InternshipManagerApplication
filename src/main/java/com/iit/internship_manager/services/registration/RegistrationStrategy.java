// RegistrationStrategy.java
package com.iit.internship_manager.services.registration;

import com.iit.internship_manager.domain.enums.UserType;
import com.iit.internship_manager.web.dtos.AuthResponse;
import com.iit.internship_manager.web.dtos.registration.RegisterRequest;

public interface RegistrationStrategy<T extends RegisterRequest> {

    // The factory uses this to find the right strategy for a given userType
    UserType getSupportedType();

    // Validates, maps, persists, and returns the response
    AuthResponse register(T request);
}