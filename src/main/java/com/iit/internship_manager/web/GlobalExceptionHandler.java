package com.iit.internship_manager.web;

import com.iit.internship_manager.domain.exceptions.*;
import com.iit.internship_manager.web.dtos.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 1. DOMAIN ERRORS (Business Rules) ---

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", "Ressource introuvable", ex.getMessage());
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainErrors(DomainException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_ERROR", "Erreur métier", ex.getMessage());
    }

    // --- 2. SECURITY ERRORS ---

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(Exception ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "Authentification échouée",
                "Email ou mot de passe incorrect.");
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(Exception ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", "Accès refusé",
                "Vous n'avez pas les permissions nécessaires.");
    }

    // --- 3. INPUT, TYPE & VALIDATION ERRORS ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        StringBuilder details = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(
                error -> details.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; "));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Erreur de validation",
                details.toString());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detail = String.format("Le paramètre '%s' doit être de type '%s'",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "inconnu");
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", "Erreur de format", detail);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedJson(Exception ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "MALFORMED_JSON", "Format JSON invalide",
                "Le corps de la requête est illisible ou mal formé.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "Méthode non supportée",
                "Cette opération n'est pas autorisée pour cette route.");
    }

    // --- 4. SERVER & INFRASTRUCTURE ERRORS (THE NEW ADDITIONS) ---

    /**
     * Triggered if the Database (MySQL) is down or unreachable.
     */
    @ExceptionHandler(org.springframework.dao.DataAccessResourceFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleDatabaseDown(Exception ex) {
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "DATABASE_OFFLINE",
                "Service Temporairement Indisponible",
                "Le serveur de base de données ne répond pas. Veuillez réessayer plus tard.");
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDatabaseConflict(Exception ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "DATABASE_CONFLICT", "Conflit de données",
                "Cette information existe déjà ou viole une contrainte d'intégrité.");
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiErrorResponse> handleBug(NullPointerException ex) {
        ex.printStackTrace();
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "CODE_ERROR", "Erreur Interne",
                "Une erreur de programmation est survenue (NPE).");
    }

    // --- 5. GLOBAL FALLBACK ---

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralError(Exception ex) {
        ex.printStackTrace();
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Erreur Système",
                "Une erreur inattendue est survenue sur le serveur.");
    }

    // --- HELPER METHOD ---

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(HttpStatus status, String code, String title,
            String detail) {
        ApiErrorResponse.ErrorDetail errorDetail = ApiErrorResponse.ErrorDetail.builder()
                .status(String.valueOf(status.value()))
                .code(code)
                .title(title)
                .detail(detail)
                .build();

        return new ResponseEntity<>(
                ApiErrorResponse.builder()
                        .status("error")
                        .errors(List.of(errorDetail))
                        .meta(Map.of("help", "https://iit.tn/support"))
                        .build(),
                status);
    }
}