package com.iit.internship_manager.web.dtos;

import com.iit.internship_manager.domain.enums.Priority;
import com.iit.internship_manager.domain.enums.CreatorRole;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {
    private Long id;
    private String description;
    private boolean completed;
    private LocalDateTime deadline;
    private Priority priorite;
    private CreatorRole creePar;
    private LocalDateTime createdAt;

    // We don't send the whole Affectation object to avoid infinite recursion
    // Just the ID is enough for the frontend logic
    private Long affectationId;
}