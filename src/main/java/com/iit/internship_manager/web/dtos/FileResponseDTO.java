package com.iit.internship_manager.web.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponseDTO {
    private Long id;
    private String originalName;
    private String fileType;
    private long size;
    private LocalDateTime uploadTime;
    private Long affectationId;
    private String ownerName; // We will fill this with the Full Name
}