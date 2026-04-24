package com.iit.internship_manager.web;

import com.iit.internship_manager.domain.exceptions.*;
import com.iit.internship_manager.web.dtos.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}