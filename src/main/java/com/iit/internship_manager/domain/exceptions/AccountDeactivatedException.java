package com.iit.internship_manager.domain.exceptions;

public class AccountDeactivatedException extends DomainException {
    public AccountDeactivatedException(String message) {
        super(message);
    }
}