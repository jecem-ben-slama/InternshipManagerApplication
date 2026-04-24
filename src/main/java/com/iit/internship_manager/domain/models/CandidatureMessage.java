package com.iit.internship_manager.domain.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidature_messages")
@Getter
@Setter
@NoArgsConstructor
public class CandidatureMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Candidature candidature;
    @ManyToOne(fetch = FetchType.LAZY)
    private Utilisateur sender;
    @Column(columnDefinition = "TEXT")
    private String content;
    private String fileLink;
    private LocalDateTime sentAt = LocalDateTime.now();
}