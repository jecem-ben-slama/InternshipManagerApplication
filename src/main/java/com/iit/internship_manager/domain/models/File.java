package com.iit.internship_manager.domain.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalName;
    private String storedName;
    private String fileType;
    private long size;
    private LocalDateTime uploadTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affectation_id")
    private Affectation affectation;

    @ManyToOne(fetch = FetchType.LAZY) // Use Lazy to keep it performant
    @JoinColumn(name = "owner_id")
    private Utilisateur owner;
}