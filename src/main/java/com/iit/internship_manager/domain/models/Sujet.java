package com.iit.internship_manager.domain.models;

import com.iit.internship_manager.domain.enums.SujetStatus;
import jakarta.persistence.*;
import lombok.*;

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
    private SujetStatus statut = SujetStatus.AVAILABLE;
    @ManyToOne(fetch = FetchType.LAZY)
    private Enseignant proposant;
}