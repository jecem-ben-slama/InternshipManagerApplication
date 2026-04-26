package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.Candidature;
import com.iit.internship_manager.domain.models.CandidatureMessage;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
import com.iit.internship_manager.domain.exceptions.UnauthorizedActionException;
import com.iit.internship_manager.repositories.CandidatureRepository;
import com.iit.internship_manager.repositories.MessageRepository;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.web.dtos.MessageRequest;
import com.iit.internship_manager.web.dtos.MessageResponseDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final CandidatureRepository candidatureRepository;
    private final UserRepository userRepository;

    /**
     * Sends a message within a specific candidature context.
     * Validates that the sender is either the student or the supervisor.
     */
    @Transactional
    public MessageResponseDTO sendMessage(Long candidatureId, MessageRequest dto) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", candidatureId));

        Utilisateur sender = getCurrentUser();

        // Security check: Is the sender involved in this specific internship
        // application?
        validateParticipant(candidature, sender);

        CandidatureMessage message = new CandidatureMessage();
        message.setCandidature(candidature);
        message.setSender(sender);
        message.setContent(dto.getContent());
        message.setFileLink(dto.getFileLink());
        message.setSentAt(LocalDateTime.now());

        return MessageResponseDTO.fromEntity(messageRepository.save(message));
    }

    /**
     * Retrieves the entire chat history for a candidature.
     */
    @Transactional(readOnly = true)
    public List<MessageResponseDTO> getConversation(Long candidatureId) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", candidatureId));

        validateParticipant(candidature, getCurrentUser());

        return messageRepository.findByCandidatureIdOrderBySentAtAsc(candidatureId)
                .stream()
                .map(MessageResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // --- Helpers ---

    private Utilisateur getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedActionException("Utilisateur non authentifié"));
    }

    private void validateParticipant(Candidature candidature, Utilisateur user) {
        Long studentId = candidature.getEtudiant().getId();
        Long teacherId = candidature.getSujet().getEnseignant().getId();
        Long currentUserId = user.getId();

        if (!currentUserId.equals(studentId) && !currentUserId.equals(teacherId)) {
            throw new UnauthorizedActionException("Accès refusé : vous ne participez pas à cette candidature.");
        }
    }
}