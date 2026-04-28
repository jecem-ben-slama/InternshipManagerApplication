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

    //* Get candidatures of the currently authenticated user, with optional filtering by status.
    // Accessible to both students and teachers, but shows data based on role (e.g., students see only their own candidatures, teachers see candidatures for their subjects).
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ETUDIANT', 'ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Page<CandidatureResponseDTO>>> getMyCandidatures(
            @RequestParam(required = false) DemandeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<CandidatureResponseDTO> list = candidatureService.getPagedCandidatures(status, page, size);
        return ResponseEntity.ok(ApiResponse.success("Liste des candidatures récupérée", list));
    }

   //* Syudent applies to a subject 
   // the partnerIds is optional and can be empty, but if provided, it must not contain the student's own ID and must correspond to valid students in the system.
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

  //* Accept Application
  // When a teacher accepts an application, the system should automatically create an "Affectation" linking the student group to the subject.
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