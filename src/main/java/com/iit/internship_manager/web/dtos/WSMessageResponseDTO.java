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
public class WSMessageResponseDTO {
    private Long id;
    private String content;
    private Long senderId;
    private String senderName; // To display "Ahmed Ben Salah" in the chat bubble
    private LocalDateTime sentAt;
    private String fileLink;
}