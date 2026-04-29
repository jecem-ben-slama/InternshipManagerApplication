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
    private String anneeId; // Added for the Year System
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

        // Map the Year ID from the ManyToOne relationship
        if (sujet.getAnneeUniversitaire() != null) {
            dto.setAnneeId(sujet.getAnneeUniversitaire().getId());
        }

        // Logic to find the actual creator/proposant
        if (sujet.getEnseignant() != null) {
            dto.setProposant(UserResponseDTO.fromEntity(sujet.getEnseignant()));
        } else if (sujet.getProposant() != null) {
            dto.setProposant(UserResponseDTO.fromEntity(sujet.getProposant()));
        }

        return dto;
    }
}