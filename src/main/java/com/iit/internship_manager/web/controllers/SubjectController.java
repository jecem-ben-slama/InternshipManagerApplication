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

        SujetResponseDTO response = subjectService.teacherProposeSujet(request);

        return new ResponseEntity<>(
                ApiResponse.success("Sujet d'enseignant créé et disponible", response),
                HttpStatus.CREATED);
    }

    /**
     * Flow for Students: Propose a subject to a specific teacher.
     * Enseignant_id = teacherId, Proposant_id = current student.
     */
    @PostMapping("/student-proposal/{teacherId}")
    @PreAuthorize("hasRole('ETUDIANT')")
    public ResponseEntity<ApiResponse<SujetResponseDTO>> studentPropose(
            @PathVariable Long teacherId,
            @Valid @RequestBody SujetRequest request) {

        SujetResponseDTO response = subjectService.studentProposeSujet(teacherId, request);

        return new ResponseEntity<>(
                ApiResponse.success("Proposition envoyée à l'enseignant", response),
                HttpStatus.CREATED);
    }

    /**
     * Get subjects where the specified teacher is the supervisor (enseignant_id).
     */
    /**
     * Get subjects where the CURRENT authenticated teacher is the supervisor.
     */
    @GetMapping("/my-subjects")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Page<SujetResponseDTO>>> getMySubjects(
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 10);

        // Notice we no longer pass teacherId here
        Page<SujetResponseDTO> response = subjectService.getSubjectsByCurrentTeacher(pageable);

        return ResponseEntity.ok(ApiResponse.success("Votre liste de sujets a été récupérée", response));
    }

    /**
     * Get all subjects (Admin/Teacher view).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ENSEIGNANT', 'RESPONSABLE_PFE')")
    public ResponseEntity<ApiResponse<Page<SujetResponseDTO>>> getAllSujets(
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<SujetResponseDTO> response = subjectService.findAll(pageable);

        return ResponseEntity.ok(ApiResponse.success("Tous les sujets récupérés", response));
    }

    /**
     * Filter subjects by status (AVAILABLE, PENDING, TAKEN, etc.).
     */
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Page<SujetResponseDTO>>> getByStatus(
            @RequestParam SujetStatus status,
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<SujetResponseDTO> response = subjectService.getSubjectsByStatus(status, pageable);

        return ResponseEntity.ok(ApiResponse.success("Sujets filtrés récupérés", response));
    }

    /**
     * Update status (e.g., Responsable validates or Teacher accepts student
     * proposal).
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<SujetResponseDTO>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {

        SujetResponseDTO updated = subjectService.updateSujetStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Statut mis à jour", updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SujetResponseDTO>> getById(@PathVariable Long id) {
        SujetResponseDTO response = subjectService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Détails du sujet récupérés", response));
    }

    /**
     * Edit subject: Authorized for the supervisor or the student who proposed it.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SujetResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody SujetRequest request) {

        SujetResponseDTO response = subjectService.updateSujet(id, request);
        return ResponseEntity.ok(ApiResponse.success("Sujet mis à jour avec succès", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        subjectService.deleteSujet(id);
        return ResponseEntity.ok(ApiResponse.success("Sujet supprimé", null));
    }
}