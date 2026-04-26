package com.iit.internship_manager.domain.exceptions;

public class SujetIndisponibleException extends DomainException {
    public SujetIndisponibleException() {
        super("Ce sujet n'est plus disponible pour de nouvelles candidatures.");
    }
}