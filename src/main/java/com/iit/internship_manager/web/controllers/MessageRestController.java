package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.services.interfaces.IMessageService;
import com.iit.internship_manager.web.dtos.MessageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageRestController {

    private final IMessageService messageService;

    @GetMapping("/candidature/{id}")
    public Page<MessageResponseDTO> getHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return messageService.getConversation(id, page, size);
    }
}