// EtudiantRegisterRequest.java
package com.iit.internship_manager.web.dtos;

import com.iit.internship_manager.domain.enums.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class EtudiantRegisterRequest extends RegisterRequest {

    @NotBlank
    private String matricule;

    @NotNull
    private Filiere filiere;

    @NotNull
    private AnneeEtude anneeEtude;
}