package com.iit.internship_manager.web.dtos;

import com.iit.internship_manager.domain.models.Candidature;
import com.iit.internship_manager.domain.enums.DemandeStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidatureResponseDTO {

    private Long id;
    private DemandeStatus statut;

    // Subject Details
    private Long sujetId;
    private String sujetTitre;
    private String sujetDescription;

    // Student Details
    private Long etudiantId;
    private String etudiantNom;
    private String etudiantPrenom;
    private String etudiantEmail;

    // Teacher Details (extracted from the Subject)
    private Long enseignantId;
    private String enseignantNom;
    private String enseignantEmail;

    // Messages summary
    private int messageCount;

    /**
     * Maps the JPA Entity to a flat DTO
     */
    public static CandidatureResponseDTO fromEntity(Candidature entity) {
        if (entity == null)
            return null;

        var builder = CandidatureResponseDTO.builder()
                .id(entity.getId())
                .statut(entity.getStatut());

        // Mapping Sujet info
        if (entity.getSujet() != null) {
            builder.sujetId(entity.getSujet().getId())
                    .sujetTitre(entity.getSujet().getTitre())
                    .sujetDescription(entity.getSujet().getDescription());

            // Mapping Teacher info from the Sujet's proposer
            if (entity.getSujet().getProposant() != null) {
                builder.enseignantId(entity.getSujet().getProposant().getId())
                        .enseignantNom(entity.getSujet().getProposant().getNom() + " "
                                + entity.getSujet().getProposant().getPrenom())
                        .enseignantEmail(entity.getSujet().getProposant().getEmail());
            }
        }

        // Mapping Etudiant info
        if (entity.getEtudiant() != null) {
            builder.etudiantId(entity.getEtudiant().getId())
                    .etudiantNom(entity.getEtudiant().getNom())
                    .etudiantPrenom(entity.getEtudiant().getPrenom())
                    .etudiantEmail(entity.getEtudiant().getEmail());
        }

        builder.messageCount(entity.getMessages() != null ? entity.getMessages().size() : 0);

        return builder.build();
    }
}