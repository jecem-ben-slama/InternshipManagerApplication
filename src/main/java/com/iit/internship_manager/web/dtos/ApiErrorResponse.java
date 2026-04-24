package com.iit.internship_manager.web.dtos;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class ApiErrorResponse {
    private String status; // "error"
    private List<ErrorDetail> errors;
    private Map<String, Object> meta;

    @Getter
    @Setter
    @Builder
    public static class ErrorDetail {
        private String status;
        private String code;
        private String title;
        private String detail;
        private Map<String, String> source;
    }
}