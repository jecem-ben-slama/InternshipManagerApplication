package com.iit.internship_manager.services.updateUser;

import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.web.dtos.updateUser.UpdateRequest;

public interface UserUpdateStrategy {
    /**
     * Checks if this strategy can handle the specific entity and DTO type.
     */
    boolean supports(Utilisateur user, UpdateRequest dto);

    /**
     * Maps specific fields from the DTO to the Entity.
     */
    void update(Utilisateur user, UpdateRequest dto);
}