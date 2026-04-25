package com.iit.internship_manager.web.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProposantSummaryDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String role;
}