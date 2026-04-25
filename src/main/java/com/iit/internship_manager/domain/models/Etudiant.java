package com.iit.internship_manager.domain.models;

import com.iit.internship_manager.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "etudiants")
@DiscriminatorValue("STUDENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Etudiant extends Utilisateur {

    @Column(unique = true)
    private String matricule;

    @Enumerated(EnumType.STRING)
    private Filiere filiere;

    @Enumerated(EnumType.STRING)
    private AnneeEtude anneeEtude;
}