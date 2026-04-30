package com.iit.internship_manager.domain.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // --- DOMAIN / BUSINESS ---
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "Ressource introuvable"),
    BUSINESS_RULE_ERROR(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_ERROR", "Erreur métier"),
    ACCOUNT_DEACTIVATED(HttpStatus.FORBIDDEN, "ACCOUNT_DEACTIVATED", "Compte désactivé"),
    CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "Conflit de données"),

    // --- AUTH / SECURITY ---
    BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "Authentification échouée"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "Accès refusé"),

    // --- VALIDATION ---
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Erreur de validation"),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", "Erreur de type de paramètre"),
    MALFORMED_JSON(HttpStatus.BAD_REQUEST, "MALFORMED_JSON", "Format JSON invalide"),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", "Paramètre manquant"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "Méthode non autorisée"),

    // --- INFRASTRUCTURE ---
    DATABASE_OFFLINE(HttpStatus.SERVICE_UNAVAILABLE, "DATABASE_OFFLINE", "Base de données indisponible"),
    DATABASE_CONFLICT(HttpStatus.CONFLICT, "DATABASE_CONFLICT", "Conflit de données"),

    // --- SYSTEM ---
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Erreur système");

    private final HttpStatus status;
    private final String code;
    private final String title;

    ErrorCode(HttpStatus status, String code, String title) {
        this.status = status;
        this.code = code;
        this.title = title;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }
}