package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.Candidature;
import com.iit.internship_manager.domain.models.CandidatureMessage;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.repositories.CandidatureRepository;
import com.iit.internship_manager.repositories.MessageRepository;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.services.interfaces.IMessageService;
import com.iit.internship_manager.web.dtos.MessageRequest;
import com.iit.internship_manager.web.dtos.MessageResponseDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Primary
@RequiredArgsConstructor
public class MessageServiceImpl implements IMessageService {

        private final MessageRepository messageRepository;
        private final UserRepository utilisateurRepository;
        private final CandidatureRepository candidatureRepository;
        private final SimpMessagingTemplate messagingTemplate;

        @Override
        @Transactional
        public void processWebSocketMessage(Long candidatureId, MessageRequest request, String userEmail) {
                // 1. Secure Identity Check
                Utilisateur sender = utilisateurRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new RuntimeException(
                                                "Utilisateur non trouvé avec l'email: " + userEmail));

                // 2. Validate Target Candidature
                Candidature candidature = candidatureRepository.findById(candidatureId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Candidature introuvable avec l'ID: " + candidatureId));

                // 3. Create and Save Entity
                // Using 'CandidatureMessage' specifically to avoid collision with Spring's
                // internal Message class
                CandidatureMessage msgEntity = new CandidatureMessage();
                msgEntity.setContent(request.getContent());
                msgEntity.setSender(sender);
                msgEntity.setCandidature(candidature);

                CandidatureMessage saved = messageRepository.save(msgEntity);

                // 4. Map to DTO for Broadcast
                MessageResponseDTO response = MessageResponseDTO.builder()
                                .id(saved.getId())
                                .content(saved.getContent())
                                .senderName(sender.getNom() + " " + sender.getPrenom())
                                .sentAt(saved.getSentAt())
                                .build();

                // 5. Real-time Broadcast
                messagingTemplate.convertAndSend("/topic/messages/" + candidatureId, response);
        }

        @Override
        public MessageResponseDTO sendMessage(Long candidatureId, MessageRequest dto) {
                // Implementation for standard HTTP POST if needed
                return null;
        }

        @Override
        public Page<MessageResponseDTO> getConversation(Long candidatureId, int page, int size) {
                return null;
        }

        @Override
        public Page<MessageResponseDTO> getAllMessagesByCandidature(Long candidatureId, int page, int size) {
                // Use your messageRepository to find by candidature ID and map to DTOs
                return null;
        }

        @Override
        public void deleteMessage(Long messageId) {
                messageRepository.deleteById(messageId);
        }
}