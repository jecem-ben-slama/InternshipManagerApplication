package com.iit.internship_manager.web.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicYearResponseDTO {
    private String id;
    private boolean active;
}