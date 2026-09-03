package com.iit.internship_manager.web.dtos;

import com.iit.internship_manager.domain.enums.AnneeEtude;
import com.iit.internship_manager.domain.enums.DepartmentType;
import com.iit.internship_manager.domain.enums.Filiere;
import com.iit.internship_manager.domain.models.Etudiant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String matricule;
    private DepartmentType department;
    private Filiere filiere;
    private AnneeEtude anneeEtude;

    public static EtudiantResponseDTO fromEntity(Etudiant student) {
        if (student == null) {
            return null;
        }

        return EtudiantResponseDTO.builder()
                .id(student.getId())
                .nom(student.getNom())
                .prenom(student.getPrenom())
                .email(student.getEmail())
                .matricule(student.getMatricule())
                .department(student.getDepartment())
                .filiere(student.getFiliere())
                .anneeEtude(student.getAnneeEtude())
                .build();
    }
}
