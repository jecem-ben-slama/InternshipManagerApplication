package com.iit.internship_manager.domain.models;

import com.iit.internship_manager.domain.enums.SujetStatus;
import com.iit.internship_manager.domain.enums.SujetType;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sujets")
@Getter
@Setter
@NoArgsConstructor
public class Sujet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private SujetStatus statut;

    @Enumerated(EnumType.STRING)
    private SujetType type; // PFA or PFE

    // Always the teacher (supervisor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id")
    private Enseignant enseignant;

    // Only filled if a student suggested the idea
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposant_id")
    private Etudiant proposant;

    @ElementCollection
    @CollectionTable(name = "sujet_technologies", joinColumns = @JoinColumn(name = "sujet_id"))
    @Column(name = "technologie")
    private List<String> technologies = new ArrayList<>();
}