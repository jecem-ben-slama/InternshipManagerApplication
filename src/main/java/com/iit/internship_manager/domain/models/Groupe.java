package com.iit.internship_manager.domain.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "groupes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Groupe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @Builder.Default // This ensures the list is initialized when using the Builder
    @ManyToMany
    @JoinTable(name = "groupe_etudiants", joinColumns = @JoinColumn(name = "groupe_id"), inverseJoinColumns = @JoinColumn(name = "etudiant_id"))
    private List<Etudiant> membres = new ArrayList<>();

    public void addMembre(Etudiant etudiant) {
        if (this.membres == null) {
            this.membres = new ArrayList<>();
        }
        this.membres.add(etudiant);
    }
}