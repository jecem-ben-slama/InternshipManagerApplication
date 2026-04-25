package com.iit.internship_manager.domain.models;

import com.iit.internship_manager.domain.enums.SujetStatus;
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
    @Column(name="statut",length = 50)
    private SujetStatus statut = SujetStatus.AVAILABLE;

    // The new technologies field
    @ElementCollection
    @CollectionTable(name = "sujet_technologies", joinColumns = @JoinColumn(name = "sujet_id"))
    @Column(name = "technologie")
    private List<String> technologies = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id") // Best practice to explicitly name the FK
    private Enseignant proposant;
}