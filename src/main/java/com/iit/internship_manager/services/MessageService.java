package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
import com.iit.internship_manager.domain.exceptions.UnauthorizedActionException;
import com.iit.internship_manager.domain.models.Candidature;
import com.iit.internship_manager.domain.models.CandidatureMessage;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.repositories.CandidatureRepository;
import com.iit.internship_manager.repositories.MessageRepository;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.web.dtos.MessageRequest;
import com.iit.internship_manager.web.dtos.MessageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable; // Fixes "Pageable cannot be resolved"
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final CandidatureRepository candidatureRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getConversation(Long candidatureId, int page, int size) {
        // First, fetch the candidature to validate
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", candidatureId));

        // Security check
        validateParticipant(candidature, getCurrentUser());

        // Setup pagination with sorting
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());

        // Return the mapped page
        return messageRepository.findByCandidatureId(candidatureId, pageable)
                .map(MessageResponseDTO::fromEntity);
    }

    /**
     * Fixes the error on line 119 by passing default values (0, 20)
     */
    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getAllMessagesByCandidature(Long candidatureId) {
        return getConversation(candidatureId, 0, 20);
    }

    @Transactional
    public MessageResponseDTO sendMessage(Long candidatureId, MessageRequest dto) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", candidatureId));

        Utilisateur sender = getCurrentUser();
        validateParticipant(candidature, sender);

        CandidatureMessage message = new CandidatureMessage();
        message.setCandidature(candidature);
        message.setSender(sender);
        message.setContent(dto.getContent());
        message.setFileLink(dto.getFileLink());
        message.setSentAt(LocalDateTime.now());

        return MessageResponseDTO.fromEntity(messageRepository.save(message));
    }

    @Transactional
    public void deleteMessage(Long messageId) {
        CandidatureMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

        if (!message.getSender().getId().equals(getCurrentUser().getId())) {
            throw new UnauthorizedActionException("Vous ne pouvez supprimer que vos propres messages.");
        }

        messageRepository.delete(message);
    }

    private Utilisateur getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedActionException("Utilisateur non authentifié"));
    }

    private void validateParticipant(Candidature candidature, Utilisateur user) {
        Long currentUserId = user.getId();
        boolean isTeacher = candidature.getSujet().getEnseignant().getId().equals(currentUserId);
        boolean isMember = false;

        if (candidature.getGroupe() != null && candidature.getGroupe().getMembres() != null) {
            isMember = candidature.getGroupe().getMembres().stream()
                    .anyMatch(member -> member.getId().equals(currentUserId));
        }

        if (!isTeacher && !isMember) {
            throw new UnauthorizedActionException("Accès refusé : vous ne participez pas à cette discussion.");
        }
    }
}