package com.iit.internship_manager.web.dtos;

import java.util.List;

import com.iit.internship_manager.domain.enums.AnneeEtude;
import com.iit.internship_manager.domain.enums.Filiere;
import com.iit.internship_manager.domain.enums.Role;
import com.iit.internship_manager.domain.enums.SpecialiteType;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String nom;
    private String prenom;
    private String matricule; // Specific to Etudiant
    private Filiere filiere; // From your Enums
    private Role role; // Add this!
    private AnneeEtude anneeEtude; // Specific to Etudiant
    private List<SpecialiteType> specialites;
    private boolean isResponsablePFE; // Specific to Enseignant
}