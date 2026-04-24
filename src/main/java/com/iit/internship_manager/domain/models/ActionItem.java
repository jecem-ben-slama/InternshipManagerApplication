package com.iit.internship_manager.domain.models;

import com.iit.internship_manager.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "action_items")
@Getter
@Setter
@NoArgsConstructor
public class ActionItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private boolean estComplete = false;
    private LocalDateTime deadline;
    @Enumerated(EnumType.STRING)
    private Priority priorite;
    @Enumerated(EnumType.STRING)
    private CreatorRole creePar;
    @ManyToOne(fetch = FetchType.LAZY)
    private Logbook logbook;
}