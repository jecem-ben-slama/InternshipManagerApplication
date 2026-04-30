package com.iit.internship_manager.domain.exceptions;

import com.iit.internship_manager.domain.enums.ErrorCode;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resource, Long id) {
        super(
                ErrorCode.RESOURCE_NOT_FOUND,
                String.format("%s with ID %d not found.", resource, id));
    }
}