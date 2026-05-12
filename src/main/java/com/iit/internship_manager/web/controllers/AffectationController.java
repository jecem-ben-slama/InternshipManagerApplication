package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.services.interfaces.IAffectationService;
import com.iit.internship_manager.web.dtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/affectations")
@RequiredArgsConstructor
public class AffectationController {

        private final IAffectationService affectationService;

        /**
         * Dedicated endpoint for the user's current active internship(s).
         * For students: returns their specific project for the year.
         * For teachers: returns the list of students they are currently supervising.
         */
        @GetMapping("/my-internship")
        @PreAuthorize("hasAnyRole('ETUDIANT', 'ENSEIGNANT')")
        public ResponseEntity<ApiResponse<Page<AffectationResponseDTO>>> getMyCurrentInternship(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                // We reuse the service logic which already filters by current year and user
                // role
                Page<AffectationResponseDTO> result = affectationService.getMyAffectations(PageRequest.of(page, size));

                return ResponseEntity.ok(ApiResponse.success("Données de votre stage actuel récupérées.", result));
        }
        /**
         * Get active affectations for the CURRENT academic year.
         * Role-aware: students see their own, teachers see their encadrements,
         * responsablePFE sees the whole department.
         */
        @GetMapping
        @PreAuthorize("hasAnyRole('ETUDIANT', 'ENSEIGNANT', 'ADMIN_IT')")
        public ResponseEntity<ApiResponse<Page<AffectationResponseDTO>>> getMyAffectations(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                Page<AffectationResponseDTO> result = affectationService.getMyAffectations(PageRequest.of(page, size));
                return ResponseEntity.ok(ApiResponse.success("Affectations de l'année en cours récupérées.", result));
        }

        @GetMapping("/all")
        @PreAuthorize("hasAnyRole('ETUDIANT', 'ENSEIGNANT', 'ADMIN_IT')")
        public ResponseEntity<ApiResponse<Page<AffectationResponseDTO>>> getAllAffectations(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                return getMyAffectations(page, size);
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('ETUDIANT', 'ENSEIGNANT', 'ADMIN_IT')")
        public ResponseEntity<ApiResponse<AffectationResponseDTO>> getById(@PathVariable Long id) {
                return ResponseEntity.ok(ApiResponse.success(
                                "Affectation recuperee.",
                                affectationService.getById(id)));
        }

        /**
         * ARCHIVE VIEW: Get affectations for a specific year (teachers & admins only).
         */
        @GetMapping("/archive/{yearId}")
        @PreAuthorize("hasAnyRole('ENSEIGNANT', 'ADMIN_IT')")
        public ResponseEntity<ApiResponse<Page<AffectationResponseDTO>>> getArchiveAffectations(
                        @PathVariable String yearId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                Page<AffectationResponseDTO> result = affectationService.getAffectationsByYear(yearId,
                                PageRequest.of(page, size));
                return ResponseEntity.ok(ApiResponse.success("Archives des affectations récupérées.", result));
        }

        /**
         * Students without an assignment for the CURRENT year.
         * Accessible to any ENSEIGNANT — service enforces isResponsablePFE() (entity
         * field) internally.
         */
        @GetMapping("/unassigned")
        @PreAuthorize("hasAnyRole('ENSEIGNANT', 'ADMIN_IT')")
        public ResponseEntity<ApiResponse<Page<EtudiantResponseDTO>>> getUnassignedStudents(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                Page<EtudiantResponseDTO> result = affectationService
                                .getUnassignedStudents(PageRequest.of(page, size))
                                .map(EtudiantResponseDTO::fromEntity);
                return ResponseEntity
                                .ok(ApiResponse.success("Étudiants non affectés (année en cours) récupérés.", result));
        }

        /**
         * Teacher workload stats for the CURRENT year.
         * Accessible to any ENSEIGNANT — service enforces isResponsablePFE() (entity
         * field) internally.
         */
        @GetMapping("/workload-stats")
        @PreAuthorize("hasRole('ENSEIGNANT')")
        public ResponseEntity<ApiResponse<Page<TeacherWorkloadDTO>>> getTeachersWorkload(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                Page<TeacherWorkloadDTO> result = affectationService.getTeachersWorkload(PageRequest.of(page, size));
                return ResponseEntity
                                .ok(ApiResponse.success("Charge des enseignants (année en cours) récupérée.", result));
        }

        /**
         * Detailed project view (subject, teacher, coworkers) for students and
         * teachers.
         */
        @GetMapping("/workload")
        @PreAuthorize("hasAnyRole('ETUDIANT', 'ENSEIGNANT')")
        public ResponseEntity<ApiResponse<Page<StudentWorkloadDTO>>> getWorkloadView(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                Page<StudentWorkloadDTO> result = affectationService.getWorkloadView(PageRequest.of(page, size));
                return ResponseEntity.ok(ApiResponse.success("Détails du projet récupérés.", result));
        }

        /**
         * Mark a project as completed.
         * Service uses checkResponsableAccess which only enforces department scope,
         * not responsable-only — kept as ENSEIGNANT to match original intent.
         */
        @PatchMapping("/{id}/complete")
        @PreAuthorize("hasRole('ENSEIGNANT')")
        public ResponseEntity<ApiResponse<String>> complete(@PathVariable Long id) {
                affectationService.completeProject(id);
                return ResponseEntity.ok(ApiResponse.success("Projet marqué comme terminé.", null));
        }

        /**
         * Abort an affectation.
         * Service uses checkResponsableAccess which only enforces department scope,
         * not responsable-only — kept as ENSEIGNANT to match original intent.
         */
        @DeleteMapping("/{id}/abort")
        @PreAuthorize("hasRole('ENSEIGNANT')")
        public ResponseEntity<ApiResponse<String>> abort(@PathVariable Long id) {
                affectationService.abortAffectation(id);
                return ResponseEntity.ok(ApiResponse.success("Affectation annulée.", null));
        }

        /**
         * Total assignment count for the CURRENT year.
         * Service is role-aware: responsablePFE gets dept-scoped count, others get
         * global.
         */
        @GetMapping("/stats/count")
        @PreAuthorize("hasRole('ENSEIGNANT')")
        public ResponseEntity<ApiResponse<Long>> getTotalCount() {
                return ResponseEntity.ok(ApiResponse.success(
                                "Nombre d'affectations (année en cours) récupéré.",
                                affectationService.getTotalAssignmentsCount()));
        }
}
