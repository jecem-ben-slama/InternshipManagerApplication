package com.iit.internship_manager.web.dtos;

import com.iit.internship_manager.domain.enums.SujetType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SujetRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @Builder.Default // Fix for technologies
    private List<String> technologies = new ArrayList<>();

    @NotNull(message = "Le type (PFA/PFE) est obligatoire")
    private SujetType type;

    @Builder.Default // Fix for partnerIds
    private List<Long> partnerIds = new ArrayList<>();
}