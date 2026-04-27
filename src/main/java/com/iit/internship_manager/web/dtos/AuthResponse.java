package com.iit.internship_manager.web.dtos;

import com.iit.internship_manager.domain.models.Utilisateur;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    /**
     * Nesting the DTO ensures consistency across the whole app.
     * Flutter will see this as a 'user' object inside the response.
     */
    private UserResponseDTO user;

    /**
     * Static factory method to create an AuthResponse from an Entity and a Token.
     */
    public static AuthResponse of(Utilisateur u, String token) {
        return AuthResponse.builder()
                .token(token)
                .user(UserResponseDTO.fromEntity(u))
                .build();
    }
}