package com.iit.internship_manager.web.dtos;

import java.time.LocalDateTime;

import com.iit.internship_manager.domain.models.CandidatureMessage;

public record MessageResponseDTO(
        Long id,
        String content,
        String senderName,
        Long senderId,
        String fileLink,
        LocalDateTime sentAt) {
    public static MessageResponseDTO fromEntity(CandidatureMessage msg) {
        return new MessageResponseDTO(
                msg.getId(),
                msg.getContent(),
                msg.getSender().getPrenom() + " " + msg.getSender().getNom(),
                msg.getSender().getId(),
                msg.getFileLink(),
                msg.getSentAt());
    }
}