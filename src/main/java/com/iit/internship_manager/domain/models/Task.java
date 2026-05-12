package com.iit.internship_manager.domain.models;

import com.iit.internship_manager.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    // 1. FIX: Added @Builder.Default to remove the warning
    @Builder.Default
    private boolean completed = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    // 2. Deadline is naturally optional (can be null in DB)
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    private Priority priorite;

    @Enumerated(EnumType.STRING)
    private CreatorRole creePar; // ENSEIGNANT or ETUDIANT

    @Column(name = "creator_name")
    private String creatorName; // Full name: First + Last

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "affectation_id", nullable = false)
    private Affectation affectation;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        syncCompletedFlag();
    }

    @PreUpdate
    protected void onUpdate() {
        syncCompletedFlag();
    }

    private void syncCompletedFlag() {
        this.completed = this.status == TaskStatus.COMPLETED;
    }
}
