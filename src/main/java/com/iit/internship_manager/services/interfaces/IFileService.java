package com.iit.internship_manager.services.interfaces;

import com.iit.internship_manager.domain.models.File;
import com.iit.internship_manager.web.dtos.FileResponseDTO;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
    FileResponseDTO uploadFile(MultipartFile file, Long affectationId);

    Page<FileResponseDTO> getByAffectation(Long affectationId, Pageable pageable);

    Resource downloadFile(Long fileId);

    File getFileMetadata(Long fileId);

    void deleteFile(Long fileId);
}
