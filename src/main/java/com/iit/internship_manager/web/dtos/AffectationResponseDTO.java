package com.iit.internship_manager.web.dtos;

import java.time.LocalDateTime;

import com.iit.internship_manager.domain.models.Affectation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AffectationResponseDTO {
    private Long id;
    private String sujetTitre;
    private String encadrantNom;
    private Long candidatureId; // The key to the chat history!
    private LocalDateTime dateAffectation;

    public static AffectationResponseDTO fromEntity(Affectation affectation) {
        return AffectationResponseDTO.builder()
                .id(affectation.getId())
                .sujetTitre(affectation.getSujet().getTitre())
                .encadrantNom(affectation.getEncadrant().getNom())
                .candidatureId(affectation.getOriginalCandidature().getId())
                .dateAffectation(affectation.getDateAffectation())
                .build();
    }
}