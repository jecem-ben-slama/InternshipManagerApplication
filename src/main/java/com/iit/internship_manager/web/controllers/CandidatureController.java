package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.domain.enums.DemandeStatus;
import com.iit.internship_manager.services.interfaces.ICandidatureService; // Using Interface
import com.iit.internship_manager.web.dtos.ApiResponse;
import com.iit.internship_manager.web.dtos.CandidatureResponseDTO;
import com.iit.internship_manager.web.dtos.MessageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidatures")
@RequiredArgsConstructor
@Validated
public class CandidatureController {

    private final ICandidatureService candidatureService; // Updated to Interface

    /**
     * GET /api/candidatures/me
     * Context-aware: Returns student applications or teacher received requests.
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ETUDIANT', 'ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Page<CandidatureResponseDTO>>> getMyCandidatures(
            @RequestParam(required = false) DemandeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<CandidatureResponseDTO> list = candidatureService.getPagedCandidatures(status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Liste des candidatures récupérée", list));
    }

    /**
     * POST /api/candidatures/postuler/{sujetId}
     * Standardized endpoint for applying to a specific subject.
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
     * Logic for quota management and auto-rejection is handled in the service.
     */
    @PatchMapping("/{id}/accepter")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Void>> accepter(@PathVariable Long id) {
        candidatureService.accepterEtudiant(id);
        return ResponseEntity.ok(ApiResponse.success("Candidature acceptée et affectation créée", null));
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
     * Initiates a message thread with the group for clarifications.
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
     * Allows a student to withdraw their request before it is processed.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<ApiResponse<Void>> annuler(@PathVariable Long id) {
        candidatureService.annulerCandidature(id);
        return ResponseEntity.ok(ApiResponse.success("Candidature annulée avec succès", null));
    }
}