package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.enums.ErrorCode;
import com.iit.internship_manager.domain.exceptions.DomainException;
import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
import com.iit.internship_manager.domain.models.Candidature;
import com.iit.internship_manager.domain.models.CandidatureMessage;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.repositories.CandidatureRepository;
import com.iit.internship_manager.repositories.MessageRepository;
import com.iit.internship_manager.repositories.UserRepository;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Primary
@RequiredArgsConstructor
public class MessageServiceImpl implements IMessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final CandidatureRepository candidatureRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ISecurityContext securityContext;

    @Override
    @Transactional
    public void processWebSocketMessage(Long candidatureId, MessageRequest request, String userEmail) {
        Utilisateur sender = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Utilisateur introuvable."));

        MessageResponseDTO response = saveMessage(candidatureId, request, sender);
        messagingTemplate.convertAndSend("/topic/messages/" + candidatureId, response);
    }

    @Override
    @Transactional
    public MessageResponseDTO sendMessage(Long candidatureId, MessageRequest dto) {
        Utilisateur sender = securityContext.getCurrentUser();
        MessageResponseDTO response = saveMessage(candidatureId, dto, sender);
        messagingTemplate.convertAndSend("/topic/messages/" + candidatureId, response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getConversation(Long candidatureId, int page, int size) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", candidatureId));

        validateParticipant(candidature, securityContext.getCurrentUser());

        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        return messageRepository.findByCandidatureId(candidatureId, pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponseDTO> getAllMessagesByCandidature(Long candidatureId, int page, int size) {
        return getConversation(candidatureId, page, size);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        CandidatureMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

        if (!message.getSender().getId().equals(securityContext.getCurrentUserId())) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Vous ne pouvez supprimer que vos propres messages.");
        }

        messageRepository.delete(message);
    }

    private MessageResponseDTO saveMessage(Long candidatureId, MessageRequest request, Utilisateur sender) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", candidatureId));

        validateParticipant(candidature, sender);

        CandidatureMessage message = new CandidatureMessage();
        message.setCandidature(candidature);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setFileLink(request.getFileLink());
        message.setSentAt(LocalDateTime.now());
        message.setAnneeUniversitaire(candidature.getAnneeUniversitaire());

        return mapToResponseDTO(messageRepository.save(message));
    }

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

        boolean isTeacher = candidature.getSujet().getEnseignant().getId().equals(currentUserId);
        boolean isMember = candidature.getGroupe() != null
                && candidature.getGroupe().getMembres() != null
                && candidature.getGroupe().getMembres().stream()
                        .anyMatch(member -> member.getId().equals(currentUserId));

        if (!isTeacher && !isMember) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Acces refuse : vous ne participez pas a cette discussion.");
        }
    }
}
