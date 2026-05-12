package com.iit.internship_manager.infrastucture.security;

import com.iit.internship_manager.repositories.CandidatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChatSecurityInterceptor implements ChannelInterceptor {

    private final CandidatureRepository candidatureRepository;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractBearerToken(accessor);
            if (token == null || !jwtUtils.validateToken(token)) {
                throw new AccessDeniedException("Invalid or missing WebSocket token.");
            }

            String email = jwtUtils.getEmailFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            accessor.setUser(new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()));
        }

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
                if (candidatureId == null) {
                    throw new AccessDeniedException("Invalid chat destination.");
                }

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

    private String extractBearerToken(StompHeaderAccessor accessor) {
        List<String> headers = accessor.getNativeHeader("Authorization");
        if (headers == null || headers.isEmpty()) {
            return null;
        }

        String value = headers.get(0);
        if (value == null || !value.startsWith("Bearer ")) {
            return null;
        }

        return value.substring(7);
    }

    private Long extractId(String path) {
        try {
            return Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
        } catch (Exception e) {
            return null;
        }
    }
}
