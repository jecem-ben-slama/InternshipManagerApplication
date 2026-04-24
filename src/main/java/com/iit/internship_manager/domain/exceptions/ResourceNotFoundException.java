package com.iit.internship_manager.domain.exceptions;

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s with ID %d not found.", resource, id));
    }
}