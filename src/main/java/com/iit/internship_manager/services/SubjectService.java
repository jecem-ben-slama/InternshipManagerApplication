package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.Enseignant;
import com.iit.internship_manager.domain.models.Sujet;
import com.iit.internship_manager.domain.enums.SujetStatus;
import com.iit.internship_manager.repositories.SubjectRepository;
import com.iit.internship_manager.repositories.EnseignantRepository;
import com.iit.internship_manager.web.dtos.SujetRequest;
import com.iit.internship_manager.web.dtos.SujetResponseDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // Ensure this import is here
import org.springframework.data.domain.Pageable; // Ensure this import is here
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
            .orElseThrow(() -> new RuntimeException("Enseignant non trouvé avec l'ID: " + teacherId));

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

//*get subjects by teacher 
    public Page<SujetResponseDTO> getSubjectsByTeacher(Long teacherId, Pageable pageable) {
    // Fetch the page of entities
    Page<Sujet> sujets = subjectRepository.findByProposantId(teacherId, pageable);

    // Convert each entity in the page to a DTO
    return sujets.map(SujetResponseDTO::fromEntity);
}
//* get all available subjects */
@Transactional(readOnly = true)
public Page<SujetResponseDTO> findAll(Pageable pageable) {
    return subjectRepository.findAll(pageable)
            .map(SujetResponseDTO::fromEntity);
}

//*  get subjects by status */
@Transactional(readOnly = true)
public Page<SujetResponseDTO> getSubjectsByStatus(SujetStatus status, Pageable pageable) {
    return subjectRepository.findByStatut(status, pageable)
            .map(SujetResponseDTO::fromEntity);
}
//* update sujet status */
   @Transactional
public SujetResponseDTO updateSujetStatus(Long id, SujetStatus status) {
    // 1. Get the current authenticated user from SecurityContext
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    Enseignant currentUser = enseignantRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

    // 2. The critical check: Is he a Responsable PFE?
    if (!currentUser.isResponsablePFE()) {
        throw new RuntimeException("Seul le Responsable PFE peut valider les sujets.");
    }

    // 3. Update logic
    Sujet sujet = subjectRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Sujet non trouvé"));
    
    sujet.setStatut(status);
    return SujetResponseDTO.fromEntity(subjectRepository.save(sujet));
}

    @Transactional
    public void deleteSujet(Long sujetId) {
        subjectRepository.deleteById(sujetId);
    }
}