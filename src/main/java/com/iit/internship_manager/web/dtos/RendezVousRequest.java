
package com.iit.internship_manager.web.dtos;
import lombok.*;
import java.time.LocalDateTime;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class RendezVousRequest {
    @NotNull(message = "Affectation ID is required")
    private Long affectationId;

    @NotNull(message = "Date is required")
    @Future(message = "Meeting must be in the future")
    private LocalDateTime dateHeure;

    @NotBlank(message = "Location is required")
    private String lieu;

    @NotBlank(message = "Subject is required")
    private String objet;
}