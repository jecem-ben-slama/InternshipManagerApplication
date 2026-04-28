package com.iit.internship_manager.web.dtos;

import com.iit.internship_manager.domain.enums.Filiere;
import com.iit.internship_manager.domain.models.Etudiant;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtudiantResponseDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String cin;
    private Filiere filiere; // e.g., "Génie Informatique", "Génie Civil"

    /**
     * Static factory method to convert the Entity to a DTO.
     */
    public static EtudiantResponseDTO fromEntity(Etudiant student) {
        if (student == null)
            return null;

        return EtudiantResponseDTO.builder()
                .id(student.getId())
                .nom(student.getNom())
                .prenom(student.getPrenom())
                .email(student.getEmail())
                .filiere(student.getFiliere())
                .build();
    }
}