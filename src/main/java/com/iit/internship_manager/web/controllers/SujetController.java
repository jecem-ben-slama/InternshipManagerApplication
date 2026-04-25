package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.domain.enums.SujetStatus;
import com.iit.internship_manager.domain.models.Sujet;
import com.iit.internship_manager.services.SubjectService;
import com.iit.internship_manager.web.dtos.ApiResponse;
import com.iit.internship_manager.web.dtos.SujetRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SujetController {

    private final SubjectService subjectService;

    @PostMapping("/{teacherId}")
    public ResponseEntity<ApiResponse<Sujet>> proposeSujet(
            @PathVariable Long teacherId,
            @Valid @RequestBody SujetRequest request) {
        Sujet created = subjectService.proposeSujet(teacherId, request);
        return new ResponseEntity<>(
                ApiResponse.success("Sujet proposé avec succès", created),
                HttpStatus.CREATED);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<ApiResponse<Page<Sujet>>> getByTeacher(
            @PathVariable Long teacherId,
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<Sujet> sujets = subjectService.getSubjectsByTeacher(teacherId, pageable);

        return ResponseEntity.ok(ApiResponse.success("Liste des sujets récupérée", sujets));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<Page<Sujet>>> getAvailable(
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<Sujet> sujets = subjectService.getAllValidatedSubjects(pageable);

        return ResponseEntity.ok(ApiResponse.success("Sujets disponibles récupérés", sujets));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Sujet>> updateStatus(
            @PathVariable Long id,
            @RequestParam SujetStatus status) {
        Sujet updated = subjectService.updateSujetStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Statut mis à jour", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        subjectService.deleteSujet(id);
        return ResponseEntity.ok(ApiResponse.success("Sujet supprimé", null));
    }
}