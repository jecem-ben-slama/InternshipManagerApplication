package com.iit.internship_manager.web.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentWorkloadDTO {
    private Long affectationId;
    private String status;
    private LocalDateTime dateAffectation;

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
    public static class CoworkerDTO {
        private Long id;
        private String nom;
        private String email;
    }
}