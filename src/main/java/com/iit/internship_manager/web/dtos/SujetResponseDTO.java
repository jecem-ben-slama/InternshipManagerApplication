package com.iit.internship_manager.web.dtos;

import com.iit.internship_manager.domain.models.Sujet;
import lombok.Data;
import java.util.List;

@Data
public class SujetResponseDTO {
    private Long id;
    private String titre;
    private String description;
    private String statut;
    private List<String> technologies;
    private ProposantSummaryDTO proposant;

    /**
     * Static factory method to convert Entity to DTO
     */
    public static SujetResponseDTO fromEntity(Sujet sujet) {
        SujetResponseDTO dto = new SujetResponseDTO();
        dto.setId(sujet.getId());
        dto.setTitre(sujet.getTitre());
        dto.setDescription(sujet.getDescription());
        dto.setStatut(sujet.getStatut().name());
        dto.setTechnologies(sujet.getTechnologies());

        if (sujet.getProposant() != null) {
            dto.setProposant(ProposantSummaryDTO.builder()
                    .id(sujet.getProposant().getId())
                    .nom(sujet.getProposant().getNom())
                    .prenom(sujet.getProposant().getPrenom())
                    .email(sujet.getProposant().getEmail())
                    .role(sujet.getProposant().getRole().name())
                    .build());
        }
        return dto;
    }
}