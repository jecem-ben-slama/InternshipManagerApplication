package com.iit.internship_manager.domain.exceptions;

public class QuotaExceededException extends DomainException {
    public QuotaExceededException() {
        super("Le quota d'encadrement de l'enseignant est déjà atteint.");
    }
}