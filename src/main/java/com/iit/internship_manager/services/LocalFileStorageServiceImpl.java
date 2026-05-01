package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.exceptions.DomainException;
import com.iit.internship_manager.domain.enums.ErrorCode;
import com.iit.internship_manager.services.interfaces.IFileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local")
public class LocalFileStorageServiceImpl implements IFileStorageService {

    private final Path rootLocation;

    public LocalFileStorageServiceImpl(@Value("${file.upload-dir:uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new DomainException(ErrorCode.INTERNAL_ERROR, "Could not initialize storage location");
        }
    }

    @Override
    public String store(MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        // Generate a unique name to prevent overwriting
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;

        try {
            if (file.isEmpty()) {
                throw new DomainException(ErrorCode.VALIDATION_FAILED, "Failed to store empty file.");
            }
            Files.copy(file.getInputStream(), this.rootLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new DomainException(ErrorCode.INTERNAL_ERROR, "Failed to store file " + filename);
        }
    }

    @Override
    public Resource load(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new DomainException(ErrorCode.INTERNAL_ERROR, "Error retrieving file: " + filename);
        }
    }

    @Override
    public void delete(String filename) {
        try {
            Files.deleteIfExists(this.rootLocation.resolve(filename));
        } catch (IOException e) {
            throw new DomainException(ErrorCode.INTERNAL_ERROR, "Could not delete file: " + filename);
        }
    }
}