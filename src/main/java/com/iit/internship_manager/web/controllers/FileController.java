package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.domain.models.File;
import com.iit.internship_manager.services.interfaces.IFileService;
import com.iit.internship_manager.web.dtos.ApiResponse;
import com.iit.internship_manager.web.dtos.FileResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FileController {

    private final IFileService fileService;

    /**
     * Uploads a file for a specific affectation.
     * Wrapped in ApiResponse for Flutter consistency.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileResponseDTO>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("affectationId") Long affectationId) {

        File savedFile = fileService.uploadFile(file, affectationId);
        FileResponseDTO response = mapToDTO(savedFile);

        return ResponseEntity.ok(ApiResponse.success("Fichier téléchargé avec succès", response));
    }

    /**
     * Returns a paginated list of all files for a specific internship.
     * Maps Entity Page to DTO Page to avoid Hibernate lazy-loading issues.
     */
    @GetMapping("/affectation/{affectationId}")
    public ResponseEntity<ApiResponse<Page<FileResponseDTO>>> getFilesByAffectation(
            @PathVariable Long affectationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<File> filePage = fileService.getFilesByAffectation(affectationId, page, size);

        // Use the Page.map() method to transform the content to DTOs
        Page<FileResponseDTO> response = filePage.map(this::mapToDTO);

        return ResponseEntity.ok(ApiResponse.success("Liste des fichiers récupérée", response));
    }

    /**
     * Downloads a file.
     * Note: This remains ResponseEntity<Resource> because the client needs the raw
     * stream,
     * not a JSON wrapper.
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) {
        Resource resource = fileService.downloadFile(fileId);

        // In a real-world scenario, you might fetch the filename from DB first.
        // For now, using a generic header.
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"file_" + fileId + "\"")
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * Deletes a file from the system.
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.ok(ApiResponse.success("Fichier supprimé", null));
    }

    /**
     * Private helper to convert Entity to DTO.
     * Ensures we don't send raw Hibernate proxies to Jackson.
     */
    private FileResponseDTO mapToDTO(File file) {
        return FileResponseDTO.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .fileType(file.getFileType())
                .size(file.getSize())
                .uploadTime(file.getUploadTime())
                .affectationId(file.getAffectation().getId())
                .ownerName(file.getOwner().getNom() + " " + file.getOwner().getPrenom())
                .build();
    }
}