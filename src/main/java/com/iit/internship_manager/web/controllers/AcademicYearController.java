package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.services.AcademicYearService;
import com.iit.internship_manager.web.dtos.AcademicYearCreateRequest;
import com.iit.internship_manager.web.dtos.AcademicYearResponseDTO;
import com.iit.internship_manager.web.dtos.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academic-years")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN_IT')")
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AcademicYearResponseDTO>>> getAllYears() {
        return ResponseEntity.ok(ApiResponse.success(
                "Années universitaires récupérées avec succès",
                academicYearService.getAllYears()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createYear(@Valid @RequestBody AcademicYearCreateRequest request) {
        academicYearService.createYear(request.getId());
        return ResponseEntity.ok(ApiResponse.success("Année universitaire créée avec succès", null));
    }

    @PostMapping("/{yearId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateYear(@PathVariable String yearId) {
        academicYearService.activateYear(yearId);
        return ResponseEntity.ok(ApiResponse.success("Année universitaire activée avec succès", null));
    }
}
