// UnsupportedUserTypeException.java
package com.iit.internship_manager.domain.exceptions;

import com.iit.internship_manager.domain.enums.UserType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnsupportedUserTypeException extends DomainException {
    public UnsupportedUserTypeException(UserType type) {
        super("No registration strategy found for user type: " + type);
    }
}