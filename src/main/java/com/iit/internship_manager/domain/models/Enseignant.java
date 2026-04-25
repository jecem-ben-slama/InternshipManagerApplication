package com.iit.internship_manager.domain.models;

import com.iit.internship_manager.domain.enums.SpecialiteType;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "enseignants")
@DiscriminatorValue("TEACHER")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Enseignant extends Utilisateur {
    @Column(name = "responsable_pfe")
    private boolean responsablePFE = false;

    // The maximum number of internships allowed by their contract
    @Column(name = "quota_annuel", nullable = false)
    private int quotaAnnuel;

    // The number of internships currently being managed/supervised
    @Column(name = "encadrements_actuels")
    private int encadrementsActuels = 0;

    @ElementCollection(targetClass = SpecialiteType.class)
    @CollectionTable(name = "enseignant_specialites", joinColumns = @JoinColumn(name = "enseignant_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "specialite")
    private Set<SpecialiteType> specialites = new HashSet<>();
}