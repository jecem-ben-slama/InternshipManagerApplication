package com.iit.internship_manager.web.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotAskResponse {
    private boolean configured;
    private String answer;
    private String notice;
    private boolean confirmationRequired;
    private String confirmationPrompt;
}
