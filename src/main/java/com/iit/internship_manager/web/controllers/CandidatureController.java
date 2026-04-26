package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.domain.enums.DemandeStatus;
import com.iit.internship_manager.services.CandidatureService;
import com.iit.internship_manager.web.dtos.ApiResponse;
import com.iit.internship_manager.web.dtos.CandidatureResponseDTO;
import com.iit.internship_manager.web.dtos.MessageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidatures")
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureService candidatureService;

    /**
     * GET /api/candidatures/me?status=PENDING&page=0&size=10
     * Returns applications related to the current user (Student or Teacher).
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ETUDIANT', 'ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Page<CandidatureResponseDTO>>> getMyCandidatures(
            @RequestParam(required = false) DemandeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<CandidatureResponseDTO> list = candidatureService.getPagedCandidatures(status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Mes candidatures récupérées", list));
    }

    /**
     * POST /api/candidatures/postuler/{sujetId}
     * Student creates a new application (Solo or Binôme).
     */
    @PostMapping("/postuler/{sujetId}")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<ApiResponse<Void>> postuler(
            @PathVariable Long sujetId,
            @RequestBody(required = false) List<Long> partnerIds) {

        candidatureService.postuler(sujetId, partnerIds);
        return new ResponseEntity<>(
                ApiResponse.success("Candidature soumise avec succès", null),
                HttpStatus.CREATED);
    }

    /**
     * PATCH /api/candidatures/{id}/accepter
     * Teacher accepts the group for this subject.
     */
    @PatchMapping("/{id}/accepter")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Void>> accepter(@PathVariable Long id) {
        candidatureService.accepterEtudiant(id);
        return ResponseEntity.ok(ApiResponse.success("Candidature acceptée", null));
    }

    /**
     * PATCH /api/candidatures/{id}/refuser
     */
    @PatchMapping("/{id}/refuser")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Void>> refuser(@PathVariable Long id) {
        candidatureService.refuserEtudiant(id);
        return ResponseEntity.ok(ApiResponse.success("Candidature refusée", null));
    }

    /**
     * PATCH /api/candidatures/{id}/clarifier
     * Teacher initiates a chat/clarification regarding the application.
     */
    @PatchMapping("/{id}/clarifier")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Void>> clarifier(
            @PathVariable Long id,
            @Valid @RequestBody MessageRequest request) {

        candidatureService.demanderClarification(id, request.getContent());
        return ResponseEntity.ok(ApiResponse.success("Demande de clarification envoyée", null));
    }

    /**
     * DELETE /api/candidatures/{id}
     * Student withdraws their application.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<ApiResponse<Void>> annuler(@PathVariable Long id) {
        candidatureService.annulerCandidature(id);
        return ResponseEntity.ok(ApiResponse.success("Candidature retirée", null));
    }
}