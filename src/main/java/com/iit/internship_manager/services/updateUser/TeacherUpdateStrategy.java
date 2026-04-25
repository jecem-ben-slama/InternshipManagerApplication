package com.iit.internship_manager.services.updateUser;
import com.iit.internship_manager.domain.models.Enseignant;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.web.dtos.updateUser.TeacherUpdateDTO;
import com.iit.internship_manager.web.dtos.updateUser.UpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class TeacherUpdateStrategy implements UserUpdateStrategy {

    @Override
    public boolean supports(Utilisateur user, UpdateRequest dto) {
        return user instanceof Enseignant && "TEACHER".equals(dto.getUserType());
    }

    @Override
    public void update(Utilisateur user, UpdateRequest dto) {
        Enseignant enseignant = (Enseignant) user;
        TeacherUpdateDTO tDto = (TeacherUpdateDTO) dto;

        enseignant.setResponsablePFE(tDto.isResponsablePFE());
        enseignant.setSpecialites(tDto.getSpecialites());
    }
}