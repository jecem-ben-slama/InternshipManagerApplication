// LoginResponse.java
package com.iit.internship_manager.web.dtos;

import com.iit.internship_manager.domain.enums.Role;
import com.iit.internship_manager.domain.models.Utilisateur;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private Role role;

    public static LoginResponse of(String token, Utilisateur u) {
        return LoginResponse.builder()
                .token(token)
                .id(u.getId())
                .email(u.getEmail())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .role(u.getRole())
                .build();
    }
}