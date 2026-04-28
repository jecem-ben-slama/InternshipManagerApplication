package com.iit.internship_manager.services.updateUser;

import com.iit.internship_manager.domain.enums.AnneeEtude;
import com.iit.internship_manager.domain.enums.Filiere;
import com.iit.internship_manager.domain.models.Etudiant;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.web.dtos.updateUser.StudentUpdateDTO;
import com.iit.internship_manager.web.dtos.updateUser.UpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class StudentUpdateStrategy implements UserUpdateStrategy {

    @Override
    public boolean supports(Utilisateur user, UpdateRequest dto) {
        // Using "STUDENT".equals(dto.getUserType()) is great for Jackson discriminator
        // support
        return user instanceof Etudiant && "STUDENT".equals(dto.getUserType());
    }

    @Override
    public void update(Utilisateur user, UpdateRequest dto) {
        Etudiant etudiant = (Etudiant) user;
        StudentUpdateDTO sDto = (StudentUpdateDTO) dto;

        // Update Student-specific fields only
        etudiant.setMatricule(sDto.getMatricule());

        // Using Enum.valueOf is fine, but ensure the Flutter app sends valid Enum
        // strings
        if (sDto.getFiliere() != null) {
            etudiant.setFiliere(Filiere.valueOf(sDto.getFiliere()));
        }

        if (sDto.getAnneeEtude() != null) {
            etudiant.setAnneeEtude(AnneeEtude.valueOf(sDto.getAnneeEtude()));
        }

        // Note: Department, Nom, Prenom, and Email are already updated
        // by the parent UserUpdateServiceImpl before this method is called.
    }
}