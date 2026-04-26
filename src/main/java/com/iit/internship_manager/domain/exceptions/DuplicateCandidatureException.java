package com.iit.internship_manager.domain.exceptions;

public class DuplicateCandidatureException extends DomainException {
    public DuplicateCandidatureException() {
        super("Vous avez déjà postulé à ce sujet.");
    }
}