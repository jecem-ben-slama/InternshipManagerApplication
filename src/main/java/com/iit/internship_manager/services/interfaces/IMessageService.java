package com.iit.internship_manager.services.interfaces;

import com.iit.internship_manager.web.dtos.MessageRequest;
import com.iit.internship_manager.web.dtos.MessageResponseDTO;
import org.springframework.data.domain.Page;

public interface IMessageService {
    MessageResponseDTO sendMessage(Long candidatureId, MessageRequest dto);

    Page<MessageResponseDTO> getConversation(Long candidatureId, int page, int size);

    Page<MessageResponseDTO> getAllMessagesByCandidature(Long candidatureId,int page, int size);

    void deleteMessage(Long messageId);
}