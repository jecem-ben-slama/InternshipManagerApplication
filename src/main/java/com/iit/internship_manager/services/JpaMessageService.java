package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
import com.iit.internship_manager.domain.exceptions.UnauthorizedActionException;
import com.iit.internship_manager.domain.models.*;
import com.iit.internship_manager.repositories.CandidatureRepository;
import com.iit.internship_manager.repositories.MessageRepository;
import com.iit.internship_manager.services.interfaces.IMessageService;
import com.iit.internship_manager.services.interfaces.ISecurityContext;
import com.iit.internship_manager.web.dtos.MessageRequest;
import com.iit.internship_manager.web.dtos.MessageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JpaMessageService implements IMessageService {

    private final MessageRepository messageRepository;
    private final CandidatureRepository candidatureRepository;
    private final ISecurityContext securityContext;

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getConversation(Long candidatureId, int page, int size) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", candidatureId));

        validateParticipant(candidature, securityContext.getCurrentUser());

        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());

        return messageRepository.findByCandidatureId(candidatureId, pageable)
                .map(this::mapToResponseDTO); // Updated mapping
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getAllMessagesByCandidature(Long candidatureId, int page, int size) {
        return getConversation(candidatureId, page, size);
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

        // If your entity requires the academic year (from previous models)
        // message.setAnneeUniversitaire(candidature.getAnneeUniversitaire());

        return mapToResponseDTO(messageRepository.save(message));
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        CandidatureMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

        if (!message.getSender().getId().equals(securityContext.getCurrentUserId())) {
            throw new UnauthorizedActionException("Vous ne pouvez supprimer que vos propres messages.");
        }

        messageRepository.delete(message);
    }

    /**
     * Maps the Entity to the new Builder-based DTO
     */
    private MessageResponseDTO mapToResponseDTO(CandidatureMessage message) {
        return MessageResponseDTO.builder()
                .id(message.getId())
                .content(message.getContent())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getNom() + " " + message.getSender().getPrenom())
                .sentAt(message.getSentAt())
                .fileLink(message.getFileLink())
                .build();
    }

    private void validateParticipant(Candidature candidature, Utilisateur user) {
        Long currentUserId = user.getId();

        // Check Teacher
        boolean isTeacher = candidature.getSujet().getEnseignant().getId().equals(currentUserId);

        // Check Group Members
        boolean isMember = false;
        if (candidature.getGroupe() != null && candidature.getGroupe().getMembres() != null) {
            isMember = candidature.getGroupe().getMembres().stream()
                    .anyMatch(member -> member.getId().equals(currentUserId));
        }

        if (!isTeacher && !isMember) {
            throw new UnauthorizedActionException("Accès refusé : vous ne participez pas à cette discussion.");
        }
    }
    
    @Override
    @Transactional
    public void processWebSocketMessage(Long candidatureId, MessageRequest request, String userEmail) {
       
    }
}