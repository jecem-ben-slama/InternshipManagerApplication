package com.iit.internship_manager.web;

import com.iit.internship_manager.domain.exceptions.*;
import com.iit.internship_manager.web.dtos.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Resource Not Found", ex.getMessage());
    }

    @ExceptionHandler(SujetIndisponibleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessConflict(SujetIndisponibleException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "SUJET_UNAVAILABLE", "Business Rule Violation", ex.getMessage());
    }

    // Generic fallback for unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralError(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "System Error",
                "An unexpected error occurred.");
    }
    

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(HttpStatus status, String code, String title,
            String detail) {
        ApiErrorResponse.ErrorDetail error = ApiErrorResponse.ErrorDetail.builder()
                .status(String.valueOf(status.value()))
                .code(code)
                .title(title)
                .detail(detail)
                .build();

        ApiErrorResponse response = ApiErrorResponse.builder()
                .status("error")
                .errors(List.of(error))
                .meta(Map.of("help", "https://iit.tn/support"))
                .build();

        return new ResponseEntity<>(response, status);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return errors;
    }
    
    @ExceptionHandler({ EmailAlreadyUsedException.class, MatriculeAlreadyUsedException.class })
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleConflict(RuntimeException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(UnsupportedUserTypeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleUnsupportedType(UnsupportedUserTypeException ex) {
        return Map.of("error", ex.getMessage());
    }
    
    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiErrorResponse handleInvalidCredentials(InvalidCredentialsException ex) {
        return ApiErrorResponse.builder()
                .status("error")
                .errors(List.of(
                        ApiErrorResponse.ErrorDetail.builder()
                                .status("401")
                                .code("INVALID_CREDENTIALS")
                                .title("Authentication failed")
                                .detail(ex.getMessage())
                                .build()))
                .build();
    }
    
    // 1. Handle Access Denied (Authenticated but not authorized)
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(Exception ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access Denied",
                "You do not have permission to access this resource.");
    }

    // 2. Handle Bad Credentials (Login failure)
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(Exception ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "Login Failed",
                "Invalid email or password.");
    }

    // 3. Handle Data Integrity (DB Constraints like unique email)
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            org.springframework.dao.DataIntegrityViolationException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "DB_CONFLICT", "Database Error",
                "A record with this information already exists.");
    }
    
    @ExceptionHandler(AccountDeactivatedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountDeactivated(AccountDeactivatedException ex) {
        ApiErrorResponse.ErrorDetail error = ApiErrorResponse.ErrorDetail.builder()
                .status("403")
                .code("ACCOUNT_DISABLED")
                .title("Compte Inactif")
                .detail(ex.getMessage())
                .build();

        ApiErrorResponse response = ApiErrorResponse.builder()
                .status("error")
                .errors(List.of(error))
                .build();

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
}