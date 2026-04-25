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

    @ElementCollection(targetClass = SpecialiteType.class)
    @CollectionTable(name = "enseignant_specialites", joinColumns = @JoinColumn(name = "enseignant_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "specialite")
    private Set<SpecialiteType> specialites = new HashSet<>();
}