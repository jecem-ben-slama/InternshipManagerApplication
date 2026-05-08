package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.File;
import com.iit.internship_manager.repositories.FileRepository;
import com.iit.internship_manager.services.interfaces.IFileService;
import com.iit.internship_manager.services.interfaces.IFileStorageService;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements IFileService {

    private final FileRepository fileRepository;
    private final IFileStorageService fileStorageService;

    @Override
    @Transactional
    public File uploadFile(MultipartFile file, Long ownerId) {
        // 1. Store physically using the configured strategy (Local)
        String storedName = fileStorageService.store(file);

        // 2. Create metadata using Lombok Builder
        File fileMetadata = File.builder()
                .originalName(file.getOriginalFilename())
                .storedName(storedName)
                .fileType(file.getContentType())
                .size(file.getSize())
                .uploadTime(LocalDateTime.now())
                .ownerId(ownerId)
                .build();

        return fileRepository.save(fileMetadata);
    }

    @Override
    public Resource downloadFile(String storedName) {
        return fileStorageService.load(storedName);
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId) {
        File fileMetadata = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // Remove from disk
        fileStorageService.delete(fileMetadata.getStoredName());
        // Remove from DB
        fileRepository.delete(fileMetadata);
    }
}