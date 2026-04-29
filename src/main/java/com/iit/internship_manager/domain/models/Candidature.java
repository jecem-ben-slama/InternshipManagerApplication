package com.iit.internship_manager.domain.models;

import com.iit.internship_manager.domain.enums.DemandeStatus;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "candidatures")
@Getter
@Setter
@NoArgsConstructor
public class Candidature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupe_id") // Changed from etudiant_id
    private Groupe groupe;

    @ManyToOne(fetch = FetchType.LAZY)
    private Sujet sujet;
    // Example for Sujet, but applies to Groupe, Candidature, and Affectation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annee_universitaire_id", nullable = false)
    private AcademicYear anneeUniversitaire;
    @Enumerated(EnumType.STRING)
    private DemandeStatus statut = DemandeStatus.PENDING;

    @OneToMany(mappedBy = "candidature", cascade = CascadeType.ALL)
    private List<CandidatureMessage> messages = new ArrayList<>();
}