package com.iit.internship_manager.domain.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "logbooks")
@Getter
@Setter
@NoArgsConstructor
public class Logbook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affectation_id")
    private Affectation affectation;
    @OneToMany(mappedBy = "logbook", cascade = CascadeType.ALL)
    private List<ActionItem> tasks = new ArrayList<>();
}