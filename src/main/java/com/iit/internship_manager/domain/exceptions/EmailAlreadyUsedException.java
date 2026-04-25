package com.iit.internship_manager.domain.exceptions;

public class EmailAlreadyUsedException extends DomainException {
    public EmailAlreadyUsedException(String email) {
        super("Email already registered: " + email);
    }
}