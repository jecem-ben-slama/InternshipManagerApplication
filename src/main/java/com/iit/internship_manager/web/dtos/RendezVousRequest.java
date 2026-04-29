
package com.iit.internship_manager.web.dtos;
import lombok.*;
import java.time.LocalDateTime;

@Data
public class RendezVousRequest {
    private LocalDateTime dateHeure;
    private String lieu;
    private Long affectationId;
}