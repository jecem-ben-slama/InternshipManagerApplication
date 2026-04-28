package com.iit.internship_manager.web.dtos;

import com.iit.internship_manager.domain.enums.SujetType;
import com.iit.internship_manager.domain.models.Sujet;
import lombok.Data;
import java.util.List;

@Data
public class SujetResponseDTO {
    private Long id;
    private SujetType type;
    private String titre;
    private String description;
    private String statut;
    private List<String> technologies;
    // Changed from ProposantSummaryDTO to UserResponseDTO to get full polymorphic
    // data
    private UserResponseDTO proposant;

    /**
     * Static factory method to convert Entity to DTO
     */
    public static SujetResponseDTO fromEntity(Sujet sujet) {
        SujetResponseDTO dto = new SujetResponseDTO();
        dto.setId(sujet.getId());
        dto.setType(sujet.getType());
        dto.setTitre(sujet.getTitre());
        dto.setDescription(sujet.getDescription());
        dto.setStatut(sujet.getStatut().name());
        dto.setTechnologies(sujet.getTechnologies());

        // Logic to find the actual creator/proposant
        if (sujet.getEnseignant() != null) {
            // If a teacher owns/proposed it
            dto.setProposant(UserResponseDTO.fromEntity(sujet.getEnseignant()));
        } else if (sujet.getProposant() != null) {
            // If a student suggested it
            dto.setProposant(UserResponseDTO.fromEntity(sujet.getProposant()));
        }

        return dto;
    }
}