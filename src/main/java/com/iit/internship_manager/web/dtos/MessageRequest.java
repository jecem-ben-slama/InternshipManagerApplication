package com.iit.internship_manager.web.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequest {

    @NotBlank(message = "Le contenu du message ne peut pas être vide")
    @Size(max = 2000, message = "Le message est trop long (max 2000 caractères)")
    private String content;

    private String fileLink; // Optional link for documents/attachments
}