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

    // * Create a subject directly by a teacher 
    //! May add a step for responsable PFE validation if needed.
    @PostMapping("/teacher-proposal")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<SujetResponseDTO>> teacherPropose(
            @Valid @RequestBody SujetRequest request) {

        return new ResponseEntity<>(
                ApiResponse.success("Sujet créé et disponible", subjectService.teacherProposeSujet(request)),
                HttpStatus.CREATED);
    }

    // * Create a subject proposal directly by a student to a specific teacher.
    // The teacher can then accept (status -> AVAILABLE) or reject (status -> REJECTED) the proposal. 
    // ! May add a step for responsable PFE validation if needed.
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

    // * Get subjects proposed by the currently authenticated teacher.
    @GetMapping("/my-subjects")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Page<SujetResponseDTO>>> getMySubjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Votre liste de sujets récupérée",
                subjectService.getSubjectsByCurrentTeacher(pageable)));
    }

    // * Get all subjects with pagination
    // . Accessible to all roles, but may show different data based on role (e.g., students see only AVAILABLE subjects)
    //! may be removed in the future.
    @GetMapping
    @PreAuthorize("hasAnyRole('ENSEIGNANT',  'ETUDIANT', 'ADMIN_IT')")
    public ResponseEntity<ApiResponse<Page<SujetResponseDTO>>> getAllSujets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Tous les sujets récupérés",
                subjectService.findAll(pageable)));
    }

    // * Get subjects filtered by status (e.g., AVAILABLE for students, PENDING for responsables).
    //  Accessible to all roles but shows data based on role.
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Page<SujetResponseDTO>>> getByStatus(
            @RequestParam SujetStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Sujets filtrés récupérés",
                subjectService.getSubjectsByStatus(status, pageable)));
    }

    // * Update the status of a subject (e.g., from PENDING to AVAILABLE or REJECTED)
    //! may be used in the responsable PFE validation flow if added in the future.
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<SujetResponseDTO>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Statut mis à jour",
                subjectService.updateSujetStatus(id, request.getStatus())));
    }
    //* Get subject details by ID.
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SujetResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Détails du sujet récupérés",
                subjectService.findById(id)));
    }
    
    // * Update subject details (e.g., title, description).
    // must be done by the original author
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SujetResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody SujetRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Sujet mis à jour avec succès",
                subjectService.updateSujet(id, request)));
    }

    // * Delete a subject 
    // must be done by the original author and only if the subject is not yet assigned to any student.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ENSEIGNANT')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        subjectService.deleteSujet(id);
        return ResponseEntity.ok(ApiResponse.success("Sujet supprimé", null));
    }
}