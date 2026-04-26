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

@RestController
@RequestMapping("/api/candidatures")
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureService candidatureService;

    /**
     * GET /api/candidatures/me
     * Paginated list of applications for the current User (Student or Teacher).
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
     */
    @PostMapping("/postuler/{sujetId}")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<ApiResponse<Void>> postuler(@PathVariable Long sujetId) {
        candidatureService.postuler(sujetId);
        return ResponseEntity.ok(ApiResponse.success("Candidature envoyée avec succès", null));
    }

    /**
     * PATCH /api/candidatures/{id}/accepter
     * Teacher accepts a student (Triggering quota check and auto-rejections).
     */
    @PatchMapping("/{id}/accepter")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Void>> accepter(@PathVariable Long id) {
        candidatureService.accepterEtudiant(id);
        return ResponseEntity.ok(ApiResponse.success("Candidature acceptée avec succès", null));
    }

    /**
     * PATCH /api/candidatures/{id}/refuser
     * Teacher refuses a student.
     */
    @PatchMapping("/{id}/refuser")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Void>> refuser(@PathVariable Long id) {
        candidatureService.refuserEtudiant(id);
        return ResponseEntity.ok(ApiResponse.success("Candidature refusée", null));
    }

    /**
     * PATCH /api/candidatures/{id}/clarifier
     * Teacher requests more info/clarification from the student.
     */
   @PatchMapping("/{id}/clarifier")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Void>> clarifier(
            @PathVariable Long id, 
            @Valid @RequestBody MessageRequest request) {
        
        // Pass the content from the request to the service
        candidatureService.demanderClarification(id, request.getContent());
        
        return ResponseEntity.ok(ApiResponse.success("Demande de clarification envoyée avec message", null));
    }

    /**
     * DELETE /api/candidatures/{id}
     * Student withdraws their application (Only if PENDING).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<ApiResponse<Void>> annuler(@PathVariable Long id) {
        candidatureService.annulerCandidature(id);
        return ResponseEntity.ok(ApiResponse.success("Candidature annulée avec succès", null));
    }
}