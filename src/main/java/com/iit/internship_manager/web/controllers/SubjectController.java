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


    // * propose a subject */
    @PostMapping("/{teacherId}")
    public ResponseEntity<ApiResponse<SujetResponseDTO>> proposeSujet(
            @PathVariable Long teacherId,
            @Valid @RequestBody SujetRequest request) {

        // The service now returns a SujetResponseDTO directly
        SujetResponseDTO response = subjectService.proposeSujet(teacherId, request);

        return new ResponseEntity<>(
                ApiResponse.success("Sujet proposé avec succès", response),
                HttpStatus.CREATED);
    }
  
    //* Get available subjects by teacher 
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<ApiResponse<Page<SujetResponseDTO>>> getByTeacher(
            @PathVariable Long teacherId,
            @RequestParam(defaultValue = "0") int page) { 

        // We use a fixed size of 10 as you requested
        Pageable pageable = PageRequest.of(page, 10);

        Page<SujetResponseDTO> response = subjectService.getSubjectsByTeacher(teacherId, pageable);

        return ResponseEntity.ok(ApiResponse.success("Liste des sujets récupérée", response));
    }
    // * get all  subjects 
    @GetMapping
    @PreAuthorize("hasRole('ENSEIGNANT')") 
    public ResponseEntity<ApiResponse<Page<SujetResponseDTO>>> getAllSujets(
            @RequestParam(defaultValue = "0") int page) {

        // Consistency is key: 10 items per page
        Pageable pageable = PageRequest.of(page, 10);

        Page<SujetResponseDTO> response = subjectService.findAll(pageable);

        return ResponseEntity.ok(ApiResponse.success("Tous les sujets récupérés", response));
    }
    // * get subjects by status */
   @GetMapping("/filter")
public ResponseEntity<ApiResponse<Page<SujetResponseDTO>>> getByStatus(
        @RequestParam SujetStatus status,
        @RequestParam(defaultValue = "0") int page) {

    Pageable pageable = PageRequest.of(page, 10);
    
    // We call the service method using the status from the URL
    Page<SujetResponseDTO> response = subjectService.getSubjectsByStatus(status, pageable);

    return ResponseEntity.ok(ApiResponse.success("Sujets filtrés par " + status + " récupérés", response));
}
//* update sujet status
 @PatchMapping("/{id}/status")
@PreAuthorize("hasRole('ENSEIGNANT')")
public ResponseEntity<ApiResponse<SujetResponseDTO>> updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody StatusRequest request) { // Changed to RequestBody
    
    // Pass request.getStatus() to the service
    SujetResponseDTO updated = subjectService.updateSujetStatus(id, request.getStatus());
    
    return ResponseEntity.ok(ApiResponse.success("Statut mis à jour", updated));
}
//* get sujet by id */
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<SujetResponseDTO>> getById(@PathVariable Long id) {
    SujetResponseDTO response = subjectService.findById(id);
    return ResponseEntity.ok(ApiResponse.success("Détails du sujet récupérés", response));
}
//* edit sujet */
@PutMapping("/{id}")
@PreAuthorize("hasRole('ENSEIGNANT')")
public ResponseEntity<ApiResponse<SujetResponseDTO>> update(
        @PathVariable Long id,
        @Valid @RequestBody SujetRequest request) {

    SujetResponseDTO response = subjectService.updateSujet(id, request);
    return ResponseEntity.ok(ApiResponse.success("Sujet mis à jour avec succès", response));
}
//* delete sujet */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        subjectService.deleteSujet(id);
        return ResponseEntity.ok(ApiResponse.success("Sujet supprimé", null));
    }
}