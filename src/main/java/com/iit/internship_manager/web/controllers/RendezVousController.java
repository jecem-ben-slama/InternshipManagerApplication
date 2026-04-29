package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.services.interfaces.IRendezVousService;
import com.iit.internship_manager.web.dtos.RendezVousRequest;
import com.iit.internship_manager.web.dtos.RendezVousResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class RendezVousController {

    private final IRendezVousService rendezVousService;

    // 1. Propose or Schedule a meeting
    @PostMapping
    public ResponseEntity<RendezVousResponseDTO> createMeeting(
            @RequestBody RendezVousRequest request,
            Principal principal) {
        return new ResponseEntity<>(
                rendezVousService.createMeeting(request, principal.getName()),
                HttpStatus.CREATED);
    }

    // 2. Get paginated history for an internship
    @GetMapping("/affectation/{affectationId}")
    public ResponseEntity<Page<RendezVousResponseDTO>> getMeetings(
            @PathVariable Long affectationId,
            @PageableDefault(size = 5, sort = "dateHeure") Pageable pageable) {
        return ResponseEntity.ok(rendezVousService.getAllMeetingsByAffectation(affectationId, pageable));
    }

    // 3. Confirm a meeting (Used by Student if Teacher created, or vice-versa)
    @PatchMapping("/{meetingId}/confirm")
    public ResponseEntity<RendezVousResponseDTO> confirmMeeting(
            @PathVariable Long meetingId,
            Principal principal) {
        return ResponseEntity.ok(rendezVousService.confirmMeeting(meetingId, principal.getName()));
    }

    // 4. Cancel a meeting
    @DeleteMapping("/{meetingId}")
    public ResponseEntity<Void> cancelMeeting(
            @PathVariable Long meetingId,
            Principal principal) {
        rendezVousService.cancelMeeting(meetingId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}