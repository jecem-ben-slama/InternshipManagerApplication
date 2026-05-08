package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.domain.models.File;
import com.iit.internship_manager.services.interfaces.IFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final IFileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<File> upload(@RequestParam("file") MultipartFile file,
            @RequestParam("ownerId") Long ownerId) {
        return ResponseEntity.ok(fileService.uploadFile(file, ownerId));
    }

    @GetMapping("/download/{storedName}")
    public ResponseEntity<Resource> download(@PathVariable String storedName) {
        Resource resource = fileService.downloadFile(storedName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + storedName + "\"")
                .body(resource);
    }
}