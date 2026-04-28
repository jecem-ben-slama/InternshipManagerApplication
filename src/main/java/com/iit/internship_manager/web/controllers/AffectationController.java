package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.services.interfaces.IAffectationService;
import com.iit.internship_manager.web.dtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/affectations")
@RequiredArgsConstructor
public class AffectationController {

        private final IAffectationService affectationService;

        /**
         * Standardized method to create Pageable from request params
         */
        private Pageable createPageable(int page, int size) {
                return PageRequest.of(page, size);
        }

        @GetMapping
        public ResponseEntity<ApiResponse<Page<AffectationResponseDTO>>> getMyAffectations(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                Page<AffectationResponseDTO> result = affectationService.getMyAffectations(createPageable(page, size));
                return ResponseEntity.ok(ApiResponse.success("Affectations récupérées avec succès.", result));
        }

        @GetMapping("/unassigned")
        @PreAuthorize("hasRole('ENSEIGNANT')")
        public ResponseEntity<ApiResponse<Page<EtudiantResponseDTO>>> getUnassignedStudents(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                Page<EtudiantResponseDTO> result = affectationService.getUnassignedStudents(createPageable(page, size))
                                .map(EtudiantResponseDTO::fromEntity);

                return ResponseEntity.ok(ApiResponse.success("Étudiants sans affectation récupérés.", result));
        }

        @GetMapping("/workload-stats")
        @PreAuthorize("hasRole('ENSEIGNANT')")
        public ResponseEntity<ApiResponse<Page<TeacherWorkloadDTO>>> getTeachersWorkload(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                Page<TeacherWorkloadDTO> result = affectationService.getTeachersWorkload(createPageable(page, size));
                return ResponseEntity.ok(ApiResponse.success("Charge des enseignants récupérée.", result));
        }

        @GetMapping("/workload")
        @PreAuthorize("hasAnyRole('ETUDIANT', 'ENSEIGNANT')")
        public ResponseEntity<ApiResponse<Page<StudentWorkloadDTO>>> getWorkloadView(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                Page<StudentWorkloadDTO> result = affectationService.getWorkloadView(createPageable(page, size));
                return ResponseEntity.ok(ApiResponse.success("Détails du projet récupérés.", result));
        }

        @PatchMapping("/{id}/complete")
        @PreAuthorize("hasAnyRole('RESPONSABLE', 'ENSEIGNANT')")
        public ResponseEntity<ApiResponse<String>> complete(@PathVariable Long id) {
                affectationService.completeProject(id);
                return ResponseEntity.ok(ApiResponse.success("Projet marqué comme terminé.", null));
        }

        @DeleteMapping("/{id}/abort")
        @PreAuthorize("hasRole('ENSEIGNANT')")
        public ResponseEntity<ApiResponse<String>> abort(@PathVariable Long id) {
                affectationService.abortAffectation(id);
                return ResponseEntity.ok(ApiResponse.success("Affectation annulée et quota mis à jour.", null));
        }

        @GetMapping("/stats/count")
        @PreAuthorize("hasRole('ENSEIGNANT')")
        public ResponseEntity<ApiResponse<Long>> getTotalCount() {
                return ResponseEntity.ok(ApiResponse.success(
                                "Nombre total d'affectations récupéré.",
                                affectationService.getTotalAssignmentsCount()));
        }
}