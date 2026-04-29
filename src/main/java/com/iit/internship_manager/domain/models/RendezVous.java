package com.iit.internship_manager.domain.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rendez_vous")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RendezVous {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateHeure;

    private String lieu;

    @Builder.Default
    private boolean estConfirme = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affectation_id", nullable = false)
    private Affectation affectation;
}