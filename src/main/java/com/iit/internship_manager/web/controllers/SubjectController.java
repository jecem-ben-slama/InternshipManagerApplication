package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.domain.enums.SujetStatus;
import com.iit.internship_manager.services.SubjectService;
import com.iit.internship_manager.web.dtos.ApiResponse;
import com.iit.internship_manager.web.dtos.StatusRequest;
import com.iit.internship_manager.web.dtos.SujetRequest;
import com.iit.internship_manager.web.dtos.SujetResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    /**
     * Flow for Teachers: Propose a subject that becomes AVAILABLE immediately.
     */
    @PostMapping("/teacher-proposal")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<SujetResponseDTO>> teacherPropose(
            @Valid @RequestBody SujetRequest request) {

        return new ResponseEntity<>(
                ApiResponse.success("Sujet créé et disponible", subjectService.teacherProposeSujet(request)),
                HttpStatus.CREATED);
    }

    /**
     * Flow for Students: Propose a subject to a specific teacher.
     */
    @PostMapping("/student-proposal/{teacherId}")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<ApiResponse<SujetResponseDTO>> studentPropose(
            @PathVariable Long teacherId,
            @Valid @RequestBody SujetRequest request) {

        return new ResponseEntity<>(
                ApiResponse.success("Proposition envoyée à l'enseignant",
                        subjectService.studentProposeSujet(teacherId, request)),
                HttpStatus.CREATED);
    }

    /**
     * Get subjects where the CURRENT authenticated teacher is the supervisor.
     * Standardized pagination using @RequestParam.
     */
    @GetMapping("/my-subjects")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Page<SujetResponseDTO>>> getMySubjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Votre liste de sujets récupérée",
                subjectService.getSubjectsByCurrentTeacher(pageable)));
    }

    /**
     * Get all subjects with flexible pagination.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'RESPONSABLE_PFE', 'ETUDIANT')")
    public ResponseEntity<ApiResponse<Page<SujetResponseDTO>>> getAllSujets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Tous les sujets récupérés",
                subjectService.findAll(pageable)));
    }

    /**
     * Filter subjects by status (AVAILABLE, PENDING, etc.).
     */
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Page<SujetResponseDTO>>> getByStatus(
            @RequestParam SujetStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Sujets filtrés récupérés",
                subjectService.getSubjectsByStatus(status, pageable)));
    }

    /**
     * Update status: Used by Responsable PFE to validate or reject.
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'RESPONSABLE_PFE')")
    public ResponseEntity<ApiResponse<SujetResponseDTO>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Statut mis à jour",
                subjectService.updateSujetStatus(id, request.getStatus())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SujetResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Détails du sujet récupérés",
                subjectService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SujetResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody SujetRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Sujet mis à jour avec succès",
                subjectService.updateSujet(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'RESPONSABLE_PFE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        subjectService.deleteSujet(id);
        return ResponseEntity.ok(ApiResponse.success("Sujet supprimé", null));
    }
}