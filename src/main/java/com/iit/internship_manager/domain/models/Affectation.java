package com.iit.internship_manager.domain.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "affectations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Builder.Default
    @OneToMany(mappedBy = "affectation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    // Fix for the first warning
    @Builder.Default
    private LocalDateTime dateAffectation = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "candidature_id")
    private Candidature originalCandidature;

    // Fix for the second warning
    @Builder.Default
    private String status = "IN_PROGRESS";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annee_universitaire_id", nullable = false)
    private AcademicYear anneeUniversitaire;

    /**
     * Helper method to maintain bidirectional consistency
     */
    public void addTask(Task task) {
        if (this.tasks == null) {
            this.tasks = new ArrayList<>();
        }
        this.tasks.add(task);
        task.setAffectation(this);
    }
}