package com.iit.internship_manager.web.controllers;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.iit.internship_manager.services.JpaMessageService;
import com.iit.internship_manager.web.dtos.ApiResponse;
import com.iit.internship_manager.web.dtos.MessageRequest;
import com.iit.internship_manager.web.dtos.MessageResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final JpaMessageService messageService;

    /**
     * POST /api/messages/{candidatureId}
     * Sends a new message and returns it wrapped in ApiResponse.
     */
    @PostMapping("/{candidatureId}")
    public ResponseEntity<ApiResponse<MessageResponseDTO>> sendMessage(
            @PathVariable Long candidatureId,
            @RequestBody MessageRequest request) {

        MessageResponseDTO response = messageService.sendMessage(candidatureId, request);

        return ResponseEntity.ok(ApiResponse.<MessageResponseDTO>builder()
                .success(true)
                .message("Message envoyé avec succès")
                .data(response)
                .build());
    }

    /**
     * GET /api/messages/{candidatureId}
     * Retrieves the chat history wrapped in ApiResponse.
     */
 
    
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessage(messageId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Message supprimé avec succès")
                .build());
    }
    
    @GetMapping("/{candidatureId}/all")
    public ResponseEntity<ApiResponse<Page<MessageResponseDTO>>> getAllMessages(
            @PathVariable Long candidatureId) {

        // messageService.getAllMessagesByCandidature now returns
        // Page<MessageResponseDTO>
        Page<MessageResponseDTO> response = messageService.getAllMessagesByCandidature(candidatureId);

        return ResponseEntity.ok(ApiResponse.<Page<MessageResponseDTO>>builder()
                .success(true)
                .message("Historique complet récupéré")
                .data(response)
                .build());
    }
}