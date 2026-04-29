package com.iit.internship_manager.web.dtos;

import java.time.LocalDateTime;
import com.iit.internship_manager.domain.models.Affectation;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AffectationResponseDTO {
    private Long id;
    private String sujetTitre;
    private String encadrantNom;
    private Long candidatureId; // The key to the chat history!
    private LocalDateTime dateAffectation;
    private String anneeId; // Added for the Year System (e.g., "2025-2026")

    public static AffectationResponseDTO fromEntity(Affectation affectation) {
        return AffectationResponseDTO.builder()
                .id(affectation.getId())
                .sujetTitre(affectation.getSujet().getTitre())
                .encadrantNom(affectation.getEncadrant().getNom())
                .candidatureId(affectation.getOriginalCandidature().getId())
                .dateAffectation(affectation.getDateAffectation())
                // Accessing the ID of the ManyToOne relationship
                .anneeId(affectation.getAnneeUniversitaire().getId())
                .build();
    }
}