package com.iit.internship_manager.web.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AcademicYearCreateRequest {
    @NotBlank
    private String id;
}
