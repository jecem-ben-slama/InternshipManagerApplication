package com.iit.internship_manager.web.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WSMessageRequest {
    private String content;
    private Long senderId;
    private String fileLink; // Optional: for attachments
}