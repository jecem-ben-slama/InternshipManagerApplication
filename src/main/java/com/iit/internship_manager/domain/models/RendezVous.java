package com.iit.internship_manager.domain.models;

import com.iit.internship_manager.domain.enums.CreatorRole;
import com.iit.internship_manager.domain.enums.MeetingStatus;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RendezVous {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateHeure;
    private String lieu;
    private String objet;
    private String creatorName;

    @Enumerated(EnumType.STRING)
    private CreatorRole creePar;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MeetingStatus status = MeetingStatus.PENDING;

    @Column(name = "est_confirme", nullable = false)
    @Builder.Default
    private boolean estConfirme = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affectation_id")
    private Affectation affectation;

    @PrePersist
    @PreUpdate
    void syncLegacyConfirmedFlag() {
        this.estConfirme = this.status == MeetingStatus.CONFIRMED;
    }
}
