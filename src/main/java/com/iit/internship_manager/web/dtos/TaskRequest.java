package com.iit.internship_manager.web.dtos;

import com.iit.internship_manager.domain.enums.Priority;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    private String description;
    private LocalDateTime deadline;
    private Priority priorite;
    private Long affectationId;
}