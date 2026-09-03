package com.iit.internship_manager.web.dtos;

import java.time.LocalDateTime;
import java.util.List;

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
    private String groupeNom;
    private String sujetTitre;
    private String sujetDescription;
    private String encadrantNom;
    private String encadrantEmail;
    private Long candidatureId; // The key to the chat history!
    private LocalDateTime dateAffectation;
    private String status;
    private String anneeId; // Added for the Year System (e.g., "2025-2026")
    private List<EtudiantResponseDTO> students; // List of students for this affectation (for teachers)

    public static AffectationResponseDTO fromEntity(Affectation affectation) {
        return AffectationResponseDTO.builder()
                .id(affectation.getId())
                .groupeNom(affectation.getGroupe() != null && affectation.getGroupe().getNom() != null
                        ? affectation.getGroupe().getNom()
                        : "Groupe " + affectation.getId())
                .sujetTitre(affectation.getSujet().getTitre())
                .sujetDescription(affectation.getSujet().getDescription())
                .encadrantNom(affectation.getEncadrant().getNom())
                .encadrantEmail(affectation.getEncadrant().getEmail())
                .candidatureId(affectation.getOriginalCandidature().getId())
                .dateAffectation(affectation.getDateAffectation())
                .status(affectation.getStatus())
                // Accessing the ID of the ManyToOne relationship
                .anneeId(affectation.getAnneeUniversitaire().getId())
                .students(affectation.getGroupe().getMembres().stream()
                        .map(m -> EtudiantResponseDTO.builder()
                                .id(m.getId())
                                .nom(m.getNom())
                                .prenom(m.getPrenom())
                                .email(m.getEmail())
                                .build())
                        .toList())
                .build();
    }
}
