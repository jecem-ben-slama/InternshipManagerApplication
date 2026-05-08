package com.iit.internship_manager.services.interfaces;

import com.iit.internship_manager.domain.models.File;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
    File uploadFile(MultipartFile file, Long ownerId);

    Resource downloadFile(String storedName);

    void deleteFile(Long fileId);
}