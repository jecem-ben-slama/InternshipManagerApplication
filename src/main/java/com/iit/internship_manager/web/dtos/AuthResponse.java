package com.iit.internship_manager.web.dtos;

import lombok.*;
@NoArgsConstructor
@Data
public class AuthResponse {
    private String token;
    private Long id;
    private String email;
    private String role;
}