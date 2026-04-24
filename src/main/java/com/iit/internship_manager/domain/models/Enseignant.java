package com.iit.internship_manager.domain.models;

import com.iit.internship_manager.domain.enums.SpecialiteType;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "enseignants")
@Getter
@Setter
@NoArgsConstructor
public class Enseignant extends Utilisateur {
    private boolean isResponsablePFE = false;

    @ElementCollection(targetClass = SpecialiteType.class)
    @CollectionTable(name = "enseignant_specialites", joinColumns = @JoinColumn(name = "enseignant_id"))
    @Enumerated(EnumType.STRING)
    private Set<SpecialiteType> specialites = new HashSet<>();
}