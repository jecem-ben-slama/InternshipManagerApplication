package com.iit.internship_manager.services.interfaces;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

public interface IFileStorageService {
    String store(MultipartFile file);

    Resource load(String filename);

    void delete(String filename);
}