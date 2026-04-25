package com.iit.internship_manager.web.dtos.updateUser;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentUpdateDTO extends UpdateRequest {
    @NotBlank(message = "Le matricule est obligatoire")
    private String matricule;

    private String filiere;
    private String anneeEtude;
}