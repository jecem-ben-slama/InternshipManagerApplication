package com.iit.internship_manager.domain.exceptions;

/**
 * Specifically for missing database records.
 * We keep this separate to return a 404 status code instead of a 400.
 */
public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s with ID %d not found.", resource, id));
    }
}