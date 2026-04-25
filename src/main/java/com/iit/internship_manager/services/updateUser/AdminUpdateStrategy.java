package com.iit.internship_manager.services.updateUser;
import com.iit.internship_manager.domain.models.AdminIT;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.web.dtos.updateUser.UpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class AdminUpdateStrategy implements UserUpdateStrategy {

    @Override
    public boolean supports(Utilisateur user, UpdateRequest dto) {
        return user instanceof AdminIT && "ADMIN".equals(dto.getUserType());
    }

    @Override
    public void update(Utilisateur user, UpdateRequest dto) {
        // No specific fields for AdminIT currently,
        // base fields are handled in the Service.
    }
}