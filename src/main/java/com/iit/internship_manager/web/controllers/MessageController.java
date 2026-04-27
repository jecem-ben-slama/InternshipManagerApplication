package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.services.interfaces.IMessageService; // Use Interface
import com.iit.internship_manager.web.dtos.ApiResponse;
import com.iit.internship_manager.web.dtos.MessageRequest;
import com.iit.internship_manager.web.dtos.MessageResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Validated
public class MessageController {

    private final IMessageService messageService;

    /**
     * POST /api/messages/{candidatureId}
     * Standardized for real-time chat feel in Flutter.
     */
    @PostMapping("/{candidatureId}")
    public ResponseEntity<ApiResponse<MessageResponseDTO>> sendMessage(
            @PathVariable Long candidatureId,
            @Valid @RequestBody MessageRequest request) {

        MessageResponseDTO response = messageService.sendMessage(candidatureId, request);
        return ResponseEntity.ok(ApiResponse.success("Message envoyé", response));
    }

    /**
     * GET /api/messages/{candidatureId}/all
     * Standardized pagination for chat history.
     */
    @GetMapping("/{candidatureId}/all")
    public ResponseEntity<ApiResponse<Page<MessageResponseDTO>>> getAllMessages(
            @PathVariable Long candidatureId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // Using parameters for flexibility in the Flutter ScrollController
        Page<MessageResponseDTO> response = messageService.getAllMessagesByCandidature(candidatureId, page, size);

        return ResponseEntity.ok(ApiResponse.success("Historique récupéré", response));
    }

    /**
     * DELETE /api/messages/{messageId}
     * Security check should be inside the service (only sender can delete).
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success("Message supprimé", null));
    }
}