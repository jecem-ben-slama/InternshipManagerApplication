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
    private String storedName;
    private String fileType;
    private Long size;
    private LocalDateTime uploadTime;
    private Long ownerId;
    private Long affectationId;
}
