package com.iit.internship_manager.domain.exceptions;

public class SujetIndisponibleException extends DomainException {
    public SujetIndisponibleException(Long sujetId) {
        super("Le sujet " + sujetId + " est déjà réservé ou pris.");
    }
}