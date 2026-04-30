package com.iit.internship_manager.web.dtos;

import com.iit.internship_manager.domain.enums.CreatorRole;
import com.iit.internship_manager.domain.enums.MeetingStatus;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RendezVousResponseDTO {
    private Long id;
    private LocalDateTime dateHeure;
    private String lieu;
    private String objet;
    private CreatorRole creePar;
    private String creatorName;
    private MeetingStatus status;
    private Long affectationId;
}