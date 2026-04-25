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
        return user instanceof Etudiant && "STUDENT".equals(dto.getUserType());
    }

    @Override
    public void update(Utilisateur user, UpdateRequest dto) {
        Etudiant etudiant = (Etudiant) user;
        StudentUpdateDTO sDto = (StudentUpdateDTO) dto;

        etudiant.setMatricule(sDto.getMatricule());
        if (sDto.getFiliere() != null) {
            etudiant.setFiliere(Filiere.valueOf(sDto.getFiliere()));
        }
        if (sDto.getAnneeEtude() != null) {
            etudiant.setAnneeEtude(AnneeEtude.valueOf(sDto.getAnneeEtude()));
        }
    }
}