package com.iit.internship_manager.web.dtos;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}