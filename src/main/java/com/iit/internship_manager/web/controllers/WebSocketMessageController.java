package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.web.dtos.MessageRequest;
import com.iit.internship_manager.services.interfaces.IMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller // Use @Controller for WebSockets
@RequiredArgsConstructor
public class WebSocketMessageController {

    private final IMessageService messageService;

    @MessageMapping("/chat/{candidatureId}")
    public void handleChatMessage(@DestinationVariable Long candidatureId,
            @Payload MessageRequest request,
            Principal principal) {

        // Security: Get the identity from the token, not the JSON body
        String userEmail = principal.getName();

        // Your service should now find the User by email and then save the message
        messageService.processWebSocketMessage(candidatureId, request, userEmail);
    }
}