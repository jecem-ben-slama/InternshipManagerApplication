package com.iit.internship_manager.infrastucture.security;

import com.iit.internship_manager.repositories.CandidatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class ChatSecurityInterceptor implements ChannelInterceptor {

    private final CandidatureRepository candidatureRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Check when user tries to SUBSCRIBE or SEND
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();
            if (principal == null) {
                throw new AccessDeniedException("User not authenticated");
            }

            String email = principal.getName();
            String destination = accessor.getDestination(); // e.g., /topic/messages/1 or /app/chat/1

            if (destination != null) {
                Long candidatureId = extractId(destination);

                // Security Check: Is this email linked to this candidature as Student or
                // Teacher?
                boolean authorized = candidatureRepository.existsByIdAndUserEmail(candidatureId, email);

                if (!authorized) {
                    throw new AccessDeniedException("Forbidden: You are not part of this candidature chat.");
                }
            }
        }
        return message;
    }

    private Long extractId(String path) {
        try {
            return Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
        } catch (Exception e) {
            return null;
        }
    }
}