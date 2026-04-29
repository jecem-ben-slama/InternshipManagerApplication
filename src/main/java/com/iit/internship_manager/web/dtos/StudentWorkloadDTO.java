package com.iit.internship_manager.web.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentWorkloadDTO {
    private Long affectationId;
    private String status;
    private LocalDateTime dateAffectation;
    private String anneeId; // Added: e.g., "2025-2026"

    // Subject Details
    private Long sujetId;
    private String sujetTitre;
    private String sujetDescription;

    // Teacher Details
    private String encadrantNom;
    private String encadrantEmail;

    // Coworkers (Members of the same group)
    private List<CoworkerDTO> coworkers;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CoworkerDTO {
        private Long id;
        private String nom;
        private String email;
    }
}