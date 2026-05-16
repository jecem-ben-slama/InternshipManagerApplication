package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.services.interfaces.IChatbotService;
import com.iit.internship_manager.services.interfaces.ISecurityContext;
import com.iit.internship_manager.web.dtos.ApiResponse;
import com.iit.internship_manager.web.dtos.ChatbotAskRequest;
import com.iit.internship_manager.web.dtos.ChatbotAskResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final IChatbotService chatbotService;
    private final ISecurityContext securityContext;

    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<ChatbotAskResponse>> ask(@Valid @RequestBody ChatbotAskRequest request) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        boolean confirmationRequired = chatbotService.requiresExplicitConfirmation(request.getQuestion());

        ChatbotAskResponse response = ChatbotAskResponse.builder()
                .configured(chatbotService.isConfigured())
                .answer(chatbotService.ask(
                        currentUser.getRole() != null ? currentUser.getRole().name() : "UNKNOWN",
                        currentUser.getPrenom() + " " + currentUser.getNom(),
                        request.getQuestion()))
                .notice("L assistant local IIT guide et informe. Il ne cree ni ne modifie rien sans votre confirmation explicite.")
                .confirmationRequired(confirmationRequired)
                .confirmationPrompt(confirmationRequired
                        ? "Cette demande ressemble a une action sensible. Aucune creation ou modification ne sera faite sans votre validation explicite."
                        : null)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Reponse du chatbot recuperee avec succes", response));
    }
}
