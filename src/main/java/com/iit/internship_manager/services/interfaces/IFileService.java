package com.iit.internship_manager.services.interfaces;

import com.iit.internship_manager.domain.models.File;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {

    /**
     * Uploads a file and links it to a specific internship context.
     * The ownerId is extracted internally from the security token.
     */
    File uploadFile(MultipartFile file, Long affectationId);

    /**
     * Retrieves all files associated with a specific internship group.
     */
    Page<File> getFilesByAffectation(Long affectationId, int page, int size);
    /**
     * Downloads a file using its database ID to verify access rights before
     * retrieval.
     */
    Resource downloadFile(Long fileId);

    /**
     * Removes file metadata from the database and the physical file from the disk.
     */
    void deleteFile(Long fileId);
}