package com.iit.internship_manager.domain.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
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
    private String fileType; // Keep this: e.g., "application/pdf"
    private Long size; // Keep this: Size in bytes
    private LocalDateTime uploadTime;

    // The Teacher/Supervisor/Student who uploaded the file
    private Long ownerId;

    // The specific Internship (Affectation) this file belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affectation_id")
    private Affectation affectation;
}