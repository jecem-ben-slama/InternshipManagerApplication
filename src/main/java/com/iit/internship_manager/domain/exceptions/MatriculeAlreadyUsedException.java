// MatriculeAlreadyUsedException.java
package com.iit.internship_manager.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class MatriculeAlreadyUsedException extends RuntimeException {
    public MatriculeAlreadyUsedException(String matricule) {
        super("Matricule already registered: " + matricule);
    }
}