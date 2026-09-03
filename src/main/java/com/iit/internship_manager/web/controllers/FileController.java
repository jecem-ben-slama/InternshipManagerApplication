package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.services.interfaces.IFileService;
import com.iit.internship_manager.web.dtos.FileResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final IFileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResponseDTO> upload(@RequestParam("file") MultipartFile file,
            @RequestParam("affectationId") Long affectationId) {
        return ResponseEntity.ok(fileService.uploadFile(file, affectationId));
    }

    @GetMapping("/affectation/{id}")
    public ResponseEntity<Page<FileResponseDTO>> getByAffectation(@PathVariable Long id, Pageable pageable) {
        return ResponseEntity.ok(fileService.getByAffectation(id, pageable));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Resource resource = fileService.downloadFile(id);
        FileResponseDTO metadata = FileResponseDTO.builder()
                .originalName(fileService.getFileMetadata(id).getOriginalName())
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getOriginalName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }
}
