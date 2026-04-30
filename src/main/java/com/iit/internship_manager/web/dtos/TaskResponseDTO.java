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
    private String creatorName;
    private LocalDateTime createdAt;
    private Long affectationId;
}