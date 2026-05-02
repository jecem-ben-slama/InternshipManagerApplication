package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.Affectation;
import com.iit.internship_manager.domain.models.File;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.domain.exceptions.DomainException;
import com.iit.internship_manager.domain.enums.ErrorCode;
import com.iit.internship_manager.repositories.AffectationRepository;
import com.iit.internship_manager.repositories.FileRepository;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.services.interfaces.IFileService;
import com.iit.internship_manager.services.interfaces.IFileStorageService;
import com.iit.internship_manager.services.interfaces.ISecurityContext;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements IFileService {

    private final FileRepository fileRepository;
    private final IFileStorageService fileStorageService;
    private final AffectationRepository affectationRepository;
    private final ISecurityContext securityContext;
    private final UserRepository utilisateurRepository;

    @Override
    @Transactional
    public File uploadFile(MultipartFile file, Long affectationId) {
        Affectation affectation = getAndValidateAffectation(affectationId);

        Utilisateur currentUser = utilisateurRepository.findById(securityContext.getCurrentUserId())
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        String uniqueStoredName = fileStorageService.store(file, affectationId);

        return fileRepository.save(File.builder()
                .originalName(file.getOriginalFilename())
                .storedName(uniqueStoredName)
                .fileType(file.getContentType())
                .size(file.getSize())
                .uploadTime(LocalDateTime.now())
                .owner(currentUser)
                .affectation(affectation)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<File> getFilesByAffectation(Long affectationId, int page, int size) {
        getAndValidateAffectation(affectationId);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("uploadTime").descending());
        return fileRepository.findByAffectationId(affectationId, pageRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadFile(Long fileId) {
        File metadata = fileRepository.findById(fileId)
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "File not found"));

        getAndValidateAffectation(metadata.getAffectation().getId());

        return fileStorageService.load(metadata.getStoredName());
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId) {
        File fileMetadata = fileRepository.findById(fileId)
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "File not found"));

        // 1. General Security: User must be part of the internship
        getAndValidateAffectation(fileMetadata.getAffectation().getId());

        // 2. Ownership Check: Only owner or PFE Coordinator can delete
        Long currentUserId = securityContext.getCurrentUserId();
        boolean isOwner = fileMetadata.getOwner() != null &&
                fileMetadata.getOwner().getId().equals(currentUserId);

        if (!isOwner && !securityContext.isResponsablePFE()) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Seul le propriétaire peut supprimer ce fichier");
        }

        fileStorageService.delete(fileMetadata.getStoredName());
        fileRepository.delete(fileMetadata);
    }

    /**
     * Security Check: Membership in Groupe or identity of Encadrant.
     */
    private Affectation getAndValidateAffectation(Long affectationId) {
        Affectation affectation = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Affectation not found"));

        Long currentUserId = securityContext.getCurrentUserId();

        boolean isEncadrant = affectation.getEncadrant() != null &&
                affectation.getEncadrant().getId().equals(currentUserId);

        boolean isStudentInGroup = affectation.getGroupe() != null &&
                affectation.getGroupe().getMembres().stream()
                        .anyMatch(etudiant -> etudiant.getId().equals(currentUserId));

        if (!isEncadrant && !isStudentInGroup && !securityContext.isResponsablePFE()) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Accès refusé aux fichiers de ce stage");
        }

        return affectation;
    }
}