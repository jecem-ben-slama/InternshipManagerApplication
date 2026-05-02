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
    public String store(MultipartFile file, Long affectationId) {
        // Use the filename provided by the user/frontend
        String filename = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (file.isEmpty()) {
                throw new DomainException(ErrorCode.VALIDATION_FAILED, "Failed to store empty file.");
            }

            if (filename.contains("..")) {
                // Security check: prevent directory traversal attacks
                throw new DomainException(ErrorCode.VALIDATION_FAILED,
                        "Cannot store file with relative path outside current directory.");
            }

            // 1. Create sub-directory for the specific affectation if it doesn't exist
            Path targetFolder = this.rootLocation.resolve(String.valueOf(affectationId));
            Files.createDirectories(targetFolder);

            // 2. Resolve target path using the ORIGINAL filename
            Path targetPath = targetFolder.resolve(filename);

            // 3. Copy file (Will overwrite if a file with the exact same name exists for
            // this affectation)
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path for DB: "affectationId/originalFilename.ext"
            return affectationId + "/" + filename;

        } catch (IOException e) {
            throw new DomainException(ErrorCode.INTERNAL_ERROR, "Failed to store file " + filename);
        }
    }

    @Override
    public Resource load(String storedPath) {
        try {
            Path file = rootLocation.resolve(storedPath);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Could not read file at: " + storedPath);
            }
        } catch (MalformedURLException e) {
            throw new DomainException(ErrorCode.INTERNAL_ERROR, "Error retrieving file: " + storedPath);
        }
    }

    @Override
    public void delete(String storedPath) {
        try {
            Files.deleteIfExists(this.rootLocation.resolve(storedPath));
        } catch (IOException e) {
            throw new DomainException(ErrorCode.INTERNAL_ERROR, "Could not delete file: " + storedPath);
        }
    }
}