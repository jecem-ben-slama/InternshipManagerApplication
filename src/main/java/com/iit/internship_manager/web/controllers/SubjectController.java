package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.domain.enums.SujetStatus;
import com.iit.internship_manager.services.interfaces.ISubjectService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Validated
public class SubjectController {

        private final ISubjectService subjectService;

        /**
         * Propose a subject for the CURRENT academic year.
         */
        @PostMapping("/teacher-proposal")
        @PreAuthorize("hasRole('ENSEIGNANT')")
        public ResponseEntity<ApiResponse<SujetResponseDTO>> teacherPropose(
                        @Valid @RequestBody SujetRequest request) {
                return new ResponseEntity<>(
                                ApiResponse.success("Sujet créé pour l'année en cours",
                                                subjectService.teacherProposeSujet(request)),
                                HttpStatus.CREATED);
        }

        /**
         * Student proposal for the CURRENT academic year.
         */
        @PostMapping("/student-proposal/{teacherId}")
        @PreAuthorize("hasRole('ETUDIANT')")
        public ResponseEntity<ApiResponse<SujetResponseDTO>> studentPropose(
                        @PathVariable Long teacherId,
                        @Valid @RequestBody SujetRequest request) {
                return new ResponseEntity<>(
                                ApiResponse.success("Proposition envoyée pour l'année en cours",
                                                subjectService.studentProposeSujet(teacherId, request)),
                                HttpStatus.CREATED);
        }

        /**
         * Get subjects for the current teacher in the CURRENT year.
         */
        @GetMapping("/my-subjects")
        @PreAuthorize("hasRole('ENSEIGNANT')")
        public ResponseEntity<ApiResponse<Page<SujetResponseDTO>>> getMySubjects(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                return ResponseEntity.ok(ApiResponse.success("Vos sujets de l'année en cours récupérés",
                                subjectService.getSubjectsByCurrentTeacher(pageable)));
        }

        /**
         * ARCHIVE VIEW: Get subjects for a specific academic year.
         * Useful for Responsable PFE to see 2025 subjects while in 2026.
         */
        @GetMapping("/archive/{yearId}")
        @PreAuthorize("hasAnyRole('ENSEIGNANT', 'ADMIN_IT')")
        public ResponseEntity<ApiResponse<Page<SujetResponseDTO>>> getSubjectsByYear(
                        @PathVariable String yearId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                return ResponseEntity.ok(ApiResponse.success("Archives récupérées",
                                subjectService.getSubjectsByYear(yearId, pageable)));
        }

        /**
         * Get all AVAILABLE subjects for the CURRENT year (Main Student View).
         */
        @GetMapping
        @PreAuthorize("hasAnyRole('ENSEIGNANT', 'ETUDIANT', 'ADMIN_IT')")
        public ResponseEntity<ApiResponse<Page<SujetResponseDTO>>> getAllCurrentSujets(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                return ResponseEntity.ok(ApiResponse.success("Sujets de l'année en cours récupérés",
                                subjectService.findAll(pageable)));
        }

        /**
         * Filter by status within the CURRENT year.
         */
        @GetMapping("/filter")
        public ResponseEntity<ApiResponse<Page<SujetResponseDTO>>> getByStatus(
                        @RequestParam SujetStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                return ResponseEntity.ok(ApiResponse.success("Sujets filtrés (année en cours) récupérés",
                                subjectService.getSubjectsByStatus(status, pageable)));
        }

        @PatchMapping("/{id}/status")
        @PreAuthorize("hasRole('ENSEIGNANT')")
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
        @PreAuthorize("hasRole('ENSEIGNANT')")
        public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
                subjectService.deleteSujet(id);
                return ResponseEntity.ok(ApiResponse.success("Sujet supprimé", null));
        }
}