package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.Enseignant;
import com.iit.internship_manager.domain.models.Sujet;
import com.iit.internship_manager.domain.enums.SujetStatus;
import com.iit.internship_manager.repositories.SubjectRepository;
import com.iit.internship_manager.repositories.EnseignantRepository;
import com.iit.internship_manager.web.dtos.SujetRequest;
import com.iit.internship_manager.web.dtos.SujetResponseDTO;
import com.iit.internship_manager.domain.exceptions.*; // Import your custom exception hierarchy

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final EnseignantRepository enseignantRepository;

    @Transactional
    public SujetResponseDTO proposeSujet(Long teacherId, SujetRequest dto) {
        // 1. Fetch the Enseignant
        Enseignant en = enseignantRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant", teacherId));

        // 2. Map Request DTO to Entity
        Sujet sujet = new Sujet();
        sujet.setTitre(dto.getTitre());
        sujet.setDescription(dto.getDescription());
        sujet.setTechnologies(dto.getTechnologies());
        sujet.setProposant(en);
        sujet.setStatut(SujetStatus.PENDING);

        // 3. Save the Entity
        Sujet savedSujet = subjectRepository.save(sujet);

        // 4. Return the Response DTO (The "Clean" version)
        return SujetResponseDTO.fromEntity(savedSujet);
    }

    // *get subjects by teacher
    public Page<SujetResponseDTO> getSubjectsByTeacher(Long teacherId, Pageable pageable) {
        // Fetch the page of entities
        Page<Sujet> sujets = subjectRepository.findByProposantId(teacherId, pageable);

        // Convert each entity in the page to a DTO
        return sujets.map(SujetResponseDTO::fromEntity);
    }

    // * get all available subjects */
    @Transactional(readOnly = true)
    public Page<SujetResponseDTO> findAll(Pageable pageable) {
        return subjectRepository.findAll(pageable)
                .map(SujetResponseDTO::fromEntity);
    }

    // * get subjects by status */
    @Transactional(readOnly = true)
    public Page<SujetResponseDTO> getSubjectsByStatus(SujetStatus status, Pageable pageable) {
        return subjectRepository.findByStatut(status, pageable)
                .map(SujetResponseDTO::fromEntity);
    }

    // * update sujet status */
    @Transactional
    public SujetResponseDTO updateSujetStatus(Long id, SujetStatus status) {
        // 1. Get the current authenticated user from SecurityContext
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Enseignant currentUser = enseignantRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedActionException("Utilisateur non trouvé"));

        // 2. The critical check: Is he a Responsable PFE?
        if (!currentUser.isResponsablePFE()) {
            throw new UnauthorizedActionException("Seul le Responsable PFE peut valider les sujets.");
        }

        // 3. Update logic
        Sujet sujet = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", id));

        sujet.setStatut(status);
        return SujetResponseDTO.fromEntity(subjectRepository.save(sujet));
    }

    // * get by id */
    @Transactional(readOnly = true)
    public SujetResponseDTO findById(Long id) {
        return subjectRepository.findById(id)
                .map(SujetResponseDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", id));
    }

    // * edit */
    @Transactional
    public SujetResponseDTO updateSujet(Long id, SujetRequest dto) {
        // 1. Get current user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Find the subject
        Sujet sujet = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", id));

        // 3. Security Check: Only the owner can edit
        if (!sujet.getProposant().getEmail().equals(email)) {
            throw new UnauthorizedActionException("Accès refusé: Vous n'êtes pas l'auteur de ce sujet.");
        }

        // 4. Business Rule: Can't edit if already validated/rejected
        if (sujet.getStatut() != SujetStatus.PENDING) {
            throw new SujetIndisponibleException(id);
        }

        // 5. Update fields
        sujet.setTitre(dto.getTitre());
        sujet.setDescription(dto.getDescription());
        sujet.setTechnologies(dto.getTechnologies());

        return SujetResponseDTO.fromEntity(subjectRepository.save(sujet));
    }

    // * delete */
    @Transactional
    public void deleteSujet(Long sujetId) {
        if (!subjectRepository.existsById(sujetId)) {
            throw new ResourceNotFoundException("Sujet", sujetId);
        }
        subjectRepository.deleteById(sujetId);
    }
}