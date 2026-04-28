package com.iit.internship_manager.services.updateUser;

import com.iit.internship_manager.domain.models.AdminIT;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.web.dtos.updateUser.UpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class AdminUpdateStrategy implements UserUpdateStrategy {

    @Override
    public boolean supports(Utilisateur user, UpdateRequest dto) {
        // Updated "ADMIN" to "ADMIN_IT" to match your @JsonSubTypes name
        return user instanceof AdminIT && "ADMIN_IT".equals(dto.getUserType());
    }

    @Override
    public void update(Utilisateur user, UpdateRequest dto) {
        // No specific fields for AdminIT currently.
        // Base fields (nom, prenom, email, department) are
        // already handled in UserUpdateServiceImpl.
    }
}