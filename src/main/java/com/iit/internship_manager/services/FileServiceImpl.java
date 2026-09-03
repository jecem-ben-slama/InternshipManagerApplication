package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.enums.ErrorCode;
import com.iit.internship_manager.domain.exceptions.DomainException;
import com.iit.internship_manager.domain.models.Affectation;
import com.iit.internship_manager.domain.models.File;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.repositories.AffectationRepository;
import com.iit.internship_manager.repositories.FileRepository;
import com.iit.internship_manager.services.interfaces.IFileService;
import com.iit.internship_manager.services.interfaces.IFileStorageService;
import com.iit.internship_manager.services.interfaces.ISecurityContext;
import com.iit.internship_manager.web.dtos.FileResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements IFileService {

    private final FileRepository fileRepository;
    private final AffectationRepository affectationRepository;
    private final IFileStorageService fileStorageService;
    private final ISecurityContext securityContext;

    @Override
    @Transactional
    public FileResponseDTO uploadFile(MultipartFile file, Long affectationId) {
        Affectation affectation = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Affectation introuvable."));

        Utilisateur currentUser = securityContext.getCurrentUser();
        validateParticipantAccess(affectation, currentUser);

        String storedName = fileStorageService.store(file);

        File fileMetadata = File.builder()
                .originalName(file.getOriginalFilename())
                .storedName(storedName)
                .fileType(file.getContentType())
                .size(file.getSize())
                .uploadTime(LocalDateTime.now())
                .ownerId(currentUser.getId())
                .affectation(affectation)
                .build();

        return mapToDto(fileRepository.save(fileMetadata));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileResponseDTO> getByAffectation(Long affectationId, Pageable pageable) {
        Affectation affectation = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Affectation introuvable."));

        validateParticipantAccess(affectation, securityContext.getCurrentUser());

        return fileRepository.findByAffectationIdOrderByUploadTimeDesc(affectationId, pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadFile(Long fileId) {
        File file = getAuthorizedFile(fileId);
        return fileStorageService.load(file.getStoredName());
    }

    @Override
    @Transactional(readOnly = true)
    public File getFileMetadata(Long fileId) {
        return getAuthorizedFile(fileId);
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId) {
        File fileMetadata = getAuthorizedFile(fileId);
        Utilisateur currentUser = securityContext.getCurrentUser();

        boolean isTeacher = fileMetadata.getAffectation().getEncadrant().getId().equals(currentUser.getId());
        boolean isOwner = fileMetadata.getOwnerId().equals(currentUser.getId());

        if (!isTeacher && !isOwner) {
            throw new DomainException(ErrorCode.FORBIDDEN,
                    "Seul l encadrant ou l utilisateur ayant depose le fichier peut le supprimer.");
        }

        fileStorageService.delete(fileMetadata.getStoredName());
        fileRepository.delete(fileMetadata);
    }

    private File getAuthorizedFile(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Fichier introuvable."));

        validateParticipantAccess(file.getAffectation(), securityContext.getCurrentUser());
        return file;
    }

    private void validateParticipantAccess(Affectation affectation, Utilisateur currentUser) {
        boolean isTeacher = affectation.getEncadrant().getId().equals(currentUser.getId());
        boolean isStudent = affectation.getGroupe() != null
                && affectation.getGroupe().getMembres() != null
                && affectation.getGroupe().getMembres().stream()
                        .anyMatch(member -> member.getId().equals(currentUser.getId()));

        if (!isTeacher && !isStudent && !securityContext.isAdmin()) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Acces refuse a ces fichiers.");
        }
    }

    private FileResponseDTO mapToDto(File file) {
        return FileResponseDTO.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .storedName(file.getStoredName())
                .fileType(file.getFileType())
                .size(file.getSize())
                .uploadTime(file.getUploadTime())
                .ownerId(file.getOwnerId())
                .affectationId(file.getAffectation() != null ? file.getAffectation().getId() : null)
                .build();
    }
}
