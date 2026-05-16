package com.iit.internship_manager.web.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotAskRequest {
    @NotBlank(message = "La question est obligatoire")
    private String question;
}
