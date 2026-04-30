package com.iit.internship_manager.web;

import com.iit.internship_manager.domain.enums.ErrorCode;
import com.iit.internship_manager.domain.exceptions.DomainException;
import com.iit.internship_manager.web.dtos.ApiErrorResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // --- 1. DOMAIN ERRORS ---
        @ExceptionHandler(DomainException.class)
        public ResponseEntity<ApiErrorResponse> handleDomainErrors(DomainException ex) {
                return buildErrorResponse(ex.getErrorCode(), ex.getMessage());
        }

        // --- 2. SECURITY ERRORS ---
        @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
        public ResponseEntity<ApiErrorResponse> handleBadCredentials(Exception ex) {
                return buildErrorResponse(
                                ErrorCode.BAD_CREDENTIALS,
                                "Email ou mot de passe incorrect.");
        }

        @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
        public ResponseEntity<ApiErrorResponse> handleAccessDenied(Exception ex) {
                return buildErrorResponse(
                                ErrorCode.FORBIDDEN,
                                "Vous n'avez pas les permissions nécessaires.");
        }

        // --- 3. VALIDATION & INPUT ---
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

                StringBuilder details = new StringBuilder();

                ex.getBindingResult().getFieldErrors().forEach(error -> details.append(error.getField())
                                .append(": ")
                                .append(error.getDefaultMessage())
                                .append("; "));

                return buildErrorResponse(ErrorCode.VALIDATION_FAILED, details.toString());
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

                String detail = String.format(
                                "Le paramètre '%s' doit être de type '%s'",
                                ex.getName(),
                                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "inconnu");

                return buildErrorResponse(ErrorCode.TYPE_MISMATCH, detail);
        }

        @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
        public ResponseEntity<ApiErrorResponse> handleMalformedJson(Exception ex) {
                return buildErrorResponse(
                                ErrorCode.MALFORMED_JSON,
                                "Le corps de la requête est illisible ou mal formé.");
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ApiErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {

                String detail = String.format(
                                "Le paramètre '%s' est manquant.",
                                ex.getParameterName());

                return buildErrorResponse(ErrorCode.MISSING_PARAMETER, detail);
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
                return buildErrorResponse(
                                ErrorCode.METHOD_NOT_ALLOWED,
                                "Cette opération n'est pas autorisée pour cette route.");
        }

        // --- 4. INFRA ---
        @ExceptionHandler(org.springframework.dao.DataAccessResourceFailureException.class)
        public ResponseEntity<ApiErrorResponse> handleDatabaseDown(Exception ex) {
                log.error("Database offline", ex);
                return buildErrorResponse(
                                ErrorCode.DATABASE_OFFLINE,
                                "Le serveur de base de données ne répond pas.");
        }

        @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
        public ResponseEntity<ApiErrorResponse> handleDatabaseConflict(Exception ex) {
                return buildErrorResponse(
                                ErrorCode.DATABASE_CONFLICT,
                                "Violation de contrainte d'intégrité.");
        }

        // --- 5. SYSTEM ---
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleGeneralError(Exception ex) {
                log.error("Unexpected error", ex);
                return buildErrorResponse(
                                ErrorCode.INTERNAL_ERROR,
                                "Une erreur inattendue est survenue.");
        }

        // --- HELPER ---
        private ResponseEntity<ApiErrorResponse> buildErrorResponse(ErrorCode errorCode, String detail) {

                ApiErrorResponse.ErrorDetail errorDetail = ApiErrorResponse.ErrorDetail.builder()
                                .status(String.valueOf(errorCode.getStatus().value()))
                                .code(errorCode.getCode())
                                .title(errorCode.getTitle())
                                .detail(detail)
                                .build();

                return new ResponseEntity<>(
                                ApiErrorResponse.builder()
                                                .status("error")
                                                .errors(List.of(errorDetail))
                                                .meta(Map.of("help", "https://iit.tn/support"))
                                                .build(),
                                errorCode.getStatus());
        }
}