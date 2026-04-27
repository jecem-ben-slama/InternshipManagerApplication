package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
import com.iit.internship_manager.domain.exceptions.UnauthorizedActionException;
import com.iit.internship_manager.domain.models.Candidature;
import com.iit.internship_manager.domain.models.CandidatureMessage;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.repositories.CandidatureRepository;
import com.iit.internship_manager.repositories.MessageRepository;
import com.iit.internship_manager.services.interfaces.IMessageService;
import com.iit.internship_manager.services.interfaces.ISecurityContext;
import com.iit.internship_manager.web.dtos.MessageRequest;
import com.iit.internship_manager.web.dtos.MessageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Primary
@RequiredArgsConstructor
public class JpaMessageService implements IMessageService {

    private final MessageRepository messageRepository;
    private final CandidatureRepository candidatureRepository;
    private final ISecurityContext securityContext; // Decoupled Security

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getConversation(Long candidatureId, int page, int size) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", candidatureId));

        // Use the securityContext instead of a local helper
        validateParticipant(candidature, securityContext.getCurrentUser());

        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());

        return messageRepository.findByCandidatureId(candidatureId, pageable)
                .map(MessageResponseDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getAllMessagesByCandidature(Long candidatureId, int page, int size) {
        // Standard view: Page 0 with a larger buffer
        return getConversation(candidatureId, 0, 50);
    }

    @Override
    @Transactional
    public MessageResponseDTO sendMessage(Long candidatureId, MessageRequest dto) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", candidatureId));

        Utilisateur sender = securityContext.getCurrentUser();
        validateParticipant(candidature, sender);

        CandidatureMessage message = new CandidatureMessage();
        message.setCandidature(candidature);
        message.setSender(sender);
        message.setContent(dto.getContent());
        message.setFileLink(dto.getFileLink());
        message.setSentAt(LocalDateTime.now());

        return MessageResponseDTO.fromEntity(messageRepository.save(message));
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        CandidatureMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

        // Decoupled ID check
        if (!message.getSender().getId().equals(securityContext.getCurrentUserId())) {
            throw new UnauthorizedActionException("Vous ne pouvez supprimer que vos propres messages.");
        }

        messageRepository.delete(message);
    }

    /**
     * Internal validation logic for chat participants
     */
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