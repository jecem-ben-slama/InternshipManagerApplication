package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.domain.enums.DemandeStatus;
import com.iit.internship_manager.services.CandidatureService;
import com.iit.internship_manager.web.dtos.ApiResponse;
import com.iit.internship_manager.web.dtos.CandidatureResponseDTO;
import com.iit.internship_manager.web.dtos.MessageRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
     * GET /api/candidatures/me
     * Paginated list of applications for the current User (Student or Teacher).
     * Works for groups: Students see applications of any group they belong to.
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ETUDIANT', 'ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Page<CandidatureResponseDTO>>> getMyCandidatures(
            @RequestParam(required = false) DemandeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<CandidatureResponseDTO> list = candidatureService.getPagedCandidatures(status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Liste récupérée avec succès", list));
    }

    /**
     * POST /api/candidatures/postuler/{sujetId}
     * Student applies for a specific internship topic.
     * 
     * @param partnerIds Optional list of IDs for the binôme/group members.
     */
    @PostMapping("/postuler/{sujetId}")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<ApiResponse<Void>> postuler(
            @PathVariable Long sujetId,
            @RequestBody(required = false) List<Long> partnerIds) {

        candidatureService.postuler(sujetId, partnerIds);
        return ResponseEntity.ok(ApiResponse.success("Candidature groupée envoyée avec succès", null));
    }

    /**
     * PATCH /api/candidatures/{id}/accepter
     * Teacher accepts the group. Triggers auto-rejection for all members' other
     * apps.
     */
    @PatchMapping("/{id}/accepter")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Void>> accepter(@PathVariable Long id) {
        candidatureService.accepterEtudiant(id);
        return ResponseEntity.ok(ApiResponse.success("Groupe accepté avec succès", null));
    }

    /**
     * PATCH /api/candidatures/{id}/refuser
     */
    @PatchMapping("/{id}/refuser")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Void>> refuser(@PathVariable Long id) {
        candidatureService.refuserEtudiant(id);
        return ResponseEntity.ok(ApiResponse.success("Candidature du groupe refusée", null));
    }

    /**
     * PATCH /api/candidatures/{id}/clarifier
     * Teacher starts a discussion with the group members.
     */
    @PatchMapping("/{id}/clarifier")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Void>> clarifier(
            @PathVariable Long id,
            @Valid @RequestBody MessageRequest request) {

        candidatureService.demanderClarification(id, request.getContent());
        return ResponseEntity.ok(ApiResponse.success("Demande de clarification envoyée au groupe", null));
    }

    /**
     * DELETE /api/candidatures/{id}
     * Any member of the group can withdraw the application if it's still PENDING.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<ApiResponse<Void>> annuler(@PathVariable Long id) {
        candidatureService.annulerCandidature(id);
        return ResponseEntity.ok(ApiResponse.success("Candidature annulée avec succès", null));
    }
}