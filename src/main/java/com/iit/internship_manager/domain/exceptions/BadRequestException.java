package com.iit.internship_manager.domain.exceptions;


public class BadRequestException extends DomainException {
    public BadRequestException(String message) {
        super(message);
    }
}