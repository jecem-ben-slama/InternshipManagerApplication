package com.iit.internship_manager.web.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SujetRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 150, message = "Le titre doit contenir entre 5 et 150 caractères")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 20, message = "La description doit être plus détaillée (min 20 caractères)")
    private String description;
    

    @NotEmpty(message = "Veuillez spécifier au moins une technologie")
    private List<String> technologies;
}