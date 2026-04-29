package com.iit.internship_manager.web.dtos;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RendezVousResponseDTO {
    private Long id;
    private LocalDateTime dateHeure;
    private String lieu;
    private boolean estConfirme;
    private Long affectationId;
}