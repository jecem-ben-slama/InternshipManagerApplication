package com.iit.internship_manager.web.dtos;

import com.iit.internship_manager.domain.enums.DepartmentType; // New Import
import com.iit.internship_manager.domain.enums.Role;
import com.iit.internship_manager.domain.enums.SpecialiteType;
import com.iit.internship_manager.domain.models.AdminIT;
import com.iit.internship_manager.domain.models.Enseignant;
import com.iit.internship_manager.domain.models.Etudiant;
import com.iit.internship_manager.domain.models.Utilisateur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private Role role;
    private boolean active;
    private String userType;
    private DepartmentType department; // Added centralized field
    private boolean hasProfilePhoto;

    // Student specific fields
    private String matricule;
    private String filiere;
    private String anneeEtude;

    // Teacher specific fields
    private boolean isResponsablePFE;
    private Set<SpecialiteType> specialites;
    private Integer quotaAnnuel;

    /**
     * Converts a Utilisateur entity into a UserResponseDTO based on its real type.
     */
    public static UserResponseDTO fromEntity(Utilisateur user) {
        UserResponseDTOBuilder builder = UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .role(user.getRole())
                .active(user.isActive())
                .department(user.getDepartment())
                .hasProfilePhoto(user.getProfilePhoto() != null && !user.getProfilePhoto().isBlank()); // Map the new centralized field

        if (user instanceof Etudiant etudiant) {
            builder.userType("STUDENT")
                    .matricule(etudiant.getMatricule())
                    .filiere(etudiant.getFiliere() != null ? etudiant.getFiliere().name() : null)
                    .anneeEtude(etudiant.getAnneeEtude() != null ? etudiant.getAnneeEtude().name() : null);
        } else if (user instanceof Enseignant enseignant) {
            builder.userType("TEACHER")
                    .isResponsablePFE(enseignant.isResponsablePFE())
                    .specialites(enseignant.getSpecialites())
                    .quotaAnnuel(enseignant.getQuotaAnnuel());
        } else if (user instanceof AdminIT) {
            builder.userType("ADMIN");
        }

        return builder.build();
    }
}
