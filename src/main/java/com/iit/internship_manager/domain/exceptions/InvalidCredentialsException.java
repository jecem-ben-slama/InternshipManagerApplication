package com.iit.internship_manager.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends DomainException {
    public InvalidCredentialsException() {
        // Vague on purpose — never reveal whether email or password was wrong
        super("Invalid email or password");
    }
}