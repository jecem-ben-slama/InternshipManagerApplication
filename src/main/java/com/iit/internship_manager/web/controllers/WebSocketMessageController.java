package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.services.interfaces.IMessageService;
import com.iit.internship_manager.web.dtos.MessageRequest;
import com.iit.internship_manager.web.dtos.MessageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketMessageController {

    private final IMessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    // Clients send to: /app/chat/{candidatureId}
    @MessageMapping("/chat/{candidatureId}")
    public void sendMessage(@DestinationVariable Long candidatureId, MessageRequest request) {
        // 1. Save message through the service
        MessageResponseDTO response = messageService.sendMessage(candidatureId, request);

        // 2. Broadcast to all subscribers of this candidature
        // Clients subscribe to: /topic/messages/5
        messagingTemplate.convertAndSend("/topic/messages/" + candidatureId, response);
    }
}