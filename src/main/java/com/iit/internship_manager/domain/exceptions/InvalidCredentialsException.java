package com.iit.internship_manager.domain.exceptions;

import com.iit.internship_manager.domain.enums.ErrorCode;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super(
                ErrorCode.BAD_CREDENTIALS,
                "Invalid email or password");
    }
}