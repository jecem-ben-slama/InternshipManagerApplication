package com.iit.internship_manager.services.interfaces;

import com.iit.internship_manager.domain.models.Utilisateur;

public interface ISecurityContext {
    Utilisateur getCurrentUser();

    Long getCurrentUserId();

    String getCurrentUserEmail();

    boolean hasRole(String role);
    
    boolean isResponsablePFE();
}