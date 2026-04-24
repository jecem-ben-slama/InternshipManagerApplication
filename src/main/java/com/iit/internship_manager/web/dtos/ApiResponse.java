package com.iit.internship_manager.web.dtos;

import lombok.*;
import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
public class ApiResponse<T> {
    private String status; // "success" or "error"
    private T data;
    private Map<String, Object> meta;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .data(data)
                .meta(Map.of(
                        "timestamp", Instant.now().toString(),
                        "version", "1.0"))
                .build();
    }
}