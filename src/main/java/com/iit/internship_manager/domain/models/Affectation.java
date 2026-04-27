package com.iit.internship_manager.domain.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "affectations")
@Getter
@Setter
@NoArgsConstructor
public class Affectation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupe_id")
    private Groupe groupe;

    @ManyToOne(fetch = FetchType.LAZY)
    private Enseignant encadrant;

    @OneToOne(fetch = FetchType.LAZY)
    private Sujet sujet;

    private LocalDateTime dateAffectation = LocalDateTime.now();

    @OneToOne
    @JoinColumn(name = "candidature_id")
    private Candidature originalCandidature;

    private String status = "IN_PROGRESS";
}