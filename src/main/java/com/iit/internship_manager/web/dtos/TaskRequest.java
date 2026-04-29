package com.iit.internship_manager.web.dtos;

import com.iit.internship_manager.domain.enums.Priority;
import com.iit.internship_manager.domain.enums.CreatorRole;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    private String description;

    // Optional: Teachers can set this or leave it null
    private LocalDateTime deadline;

    private Priority priorite;

    // Useful for the service logic to know who is creating it
    private CreatorRole creePar;

    // We only need the ID of the affectation to link it
    private Long affectationId;
}