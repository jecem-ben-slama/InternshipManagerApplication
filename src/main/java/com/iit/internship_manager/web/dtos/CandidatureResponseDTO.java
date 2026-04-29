package com.iit.internship_manager.web.dtos;

import com.iit.internship_manager.domain.models.Candidature;
import com.iit.internship_manager.domain.enums.DemandeStatus;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatureResponseDTO {

    private Long id;
    private DemandeStatus statut;
    private String anneeId; // Added for the Year System

    // Subject Details
    private Long sujetId;
    private String sujetTitre;
    private String sujetDescription;

    // Group Details
    private Long groupeId;
    private String groupeNom;
    private List<MembreDTO> membres;

    // Teacher Details
    private Long enseignantId;
    private String enseignantNom;
    private String enseignantEmail;

    // Messages summary
    private int messageCount;

    @Data
    @AllArgsConstructor
    public static class MembreDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
    }

    public static CandidatureResponseDTO fromEntity(Candidature entity) {
        if (entity == null)
            return null;

        var builder = CandidatureResponseDTO.builder()
                .id(entity.getId())
                .statut(entity.getStatut());

        // Mapping Year Info
        if (entity.getAnneeUniversitaire() != null) {
            builder.anneeId(entity.getAnneeUniversitaire().getId());
        }

        // Mapping Sujet & Teacher info
        if (entity.getSujet() != null) {
            builder.sujetId(entity.getSujet().getId())
                    .sujetTitre(entity.getSujet().getTitre())
                    .sujetDescription(entity.getSujet().getDescription());

            if (entity.getSujet().getEnseignant() != null) {
                builder.enseignantId(entity.getSujet().getEnseignant().getId())
                        .enseignantNom(entity.getSujet().getEnseignant().getNom() + " " +
                                entity.getSujet().getEnseignant().getPrenom())
                        .enseignantEmail(entity.getSujet().getEnseignant().getEmail());
            }
        }

        // Mapping Group Info
        if (entity.getGroupe() != null) {
            builder.groupeId(entity.getGroupe().getId())
                    .groupeNom(entity.getGroupe().getNom());

            if (entity.getGroupe().getMembres() != null) {
                List<MembreDTO> membresList = entity.getGroupe().getMembres().stream()
                        .map(m -> new MembreDTO(m.getId(), m.getNom(), m.getPrenom(), m.getEmail()))
                        .collect(Collectors.toList());
                builder.membres(membresList);
            }
        }

        builder.messageCount(entity.getMessages() != null ? entity.getMessages().size() : 0);

        return builder.build();
    }
}