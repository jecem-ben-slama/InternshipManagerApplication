package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.*;
import com.iit.internship_manager.repositories.*;
import com.iit.internship_manager.services.interfaces.IMessageService;
import com.iit.internship_manager.web.dtos.MessageRequest;
import com.iit.internship_manager.web.dtos.MessageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Primary // This tells Spring to use THIS instead of JPAMessageServiceImpl
@RequiredArgsConstructor
public class WebSocketMessageServiceImpl implements IMessageService {

    private final MessageRepository messageRepository;
    private final CandidatureRepository candidatureRepository;
    private final UserRepository utilisateurRepository;
    private final AcademicYearRepository academicYearRepository;

    @Override
    @Transactional
    public MessageResponseDTO sendMessage(Long candidatureId, MessageRequest dto) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));

        Utilisateur sender = utilisateurRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        AcademicYear activeYear = academicYearRepository.findByActiveTrue()
                .orElseThrow(() -> new RuntimeException("No active academic year"));

        CandidatureMessage message = new CandidatureMessage();
        message.setCandidature(candidature);
        message.setSender(sender);
        message.setContent(dto.getContent());
        message.setFileLink(dto.getFileLink());
        message.setAnneeUniversitaire(activeYear);
        message.setSentAt(LocalDateTime.now());

        CandidatureMessage saved = messageRepository.save(message);

        return mapToDTO(saved);
    }

    @Override
    public Page<MessageResponseDTO> getConversation(Long candidatureId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        return messageRepository.findByCandidatureId(candidatureId, pageable).map(this::mapToDTO);
    }

    @Override
    public Page<MessageResponseDTO> getAllMessagesByCandidature(Long candidatureId, int page, int size) {
        return getConversation(candidatureId, page, size);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    private MessageResponseDTO mapToDTO(CandidatureMessage msg) {
        return MessageResponseDTO.builder()
                .id(msg.getId())
                .content(msg.getContent())
                .senderId(msg.getSender().getId())
                .senderName(msg.getSender().getNom() + " " + msg.getSender().getPrenom())
                .sentAt(msg.getSentAt())
                .fileLink(msg.getFileLink())
                .build();
    }
}