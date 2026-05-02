package com.iit.internship_manager.services.interfaces;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface IFileStorageService {
    String store(MultipartFile file, Long affectationId);

    Resource load(String storedPath);

    void delete(String storedPath);
}