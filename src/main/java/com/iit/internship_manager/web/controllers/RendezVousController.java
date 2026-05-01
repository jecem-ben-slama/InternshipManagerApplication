package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.domain.enums.MeetingStatus;
import com.iit.internship_manager.services.interfaces.IRendezVousService;
import com.iit.internship_manager.web.dtos.RendezVousRequest;
import com.iit.internship_manager.web.dtos.RendezVousResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class RendezVousController {

    private final IRendezVousService rendezVousService;

    @PostMapping
    public ResponseEntity<RendezVousResponseDTO> create(@Valid @RequestBody RendezVousRequest request) {
        return new ResponseEntity<>(rendezVousService.createMeeting(request), HttpStatus.CREATED);
    }

    @GetMapping("/affectation/{id}")
    public ResponseEntity<Page<RendezVousResponseDTO>> getByAffectation(@PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(rendezVousService.getAllMeetingsByAffectation(id, pageable));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<RendezVousResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam MeetingStatus status) {
        return ResponseEntity.ok(rendezVousService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rendezVousService.delete(id);
        return ResponseEntity.noContent().build();
    }
}