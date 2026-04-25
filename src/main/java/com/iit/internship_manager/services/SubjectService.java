package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.Enseignant;
import com.iit.internship_manager.domain.models.Sujet;
import com.iit.internship_manager.domain.enums.SujetStatus;
import com.iit.internship_manager.repositories.SubjectRepository;
import com.iit.internship_manager.repositories.EnseignantRepository;
import com.iit.internship_manager.web.dtos.SujetRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // Ensure this import is here
import org.springframework.data.domain.Pageable; // Ensure this import is here
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final EnseignantRepository enseignantRepository;

    @Transactional
    public Sujet proposeSujet(Long teacherId, SujetRequest dto) {
        Enseignant en = enseignantRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé avec l'ID: " + teacherId));

        Sujet sujet = new Sujet();
        sujet.setTitre(dto.getTitre());
        sujet.setDescription(dto.getDescription());
        sujet.setTechnologies(dto.getTechnologies());
        sujet.setProposant(en);
        sujet.setStatut(SujetStatus.PENDING);

        return subjectRepository.save(sujet);
    }

    // FIX: Added Pageable parameter and passed it to the repository
    @Transactional(readOnly = true)
    public Page<Sujet> getSubjectsByTeacher(Long teacherId, Pageable pageable) {
        return subjectRepository.findByProposantId(teacherId, pageable);
    }

    // FIX: Added Pageable parameter and passed it to the repository
    @Transactional(readOnly = true)
    public Page<Sujet> getAllValidatedSubjects(Pageable pageable) {
        return subjectRepository.findByStatut(SujetStatus.VALIDATED, pageable);
    }

    @Transactional
    public Sujet updateSujetStatus(Long sujetId, SujetStatus newStatus) {
        Sujet sujet = subjectRepository.findById(sujetId)
                .orElseThrow(() -> new RuntimeException("Sujet non trouvé"));

        sujet.setStatut(newStatus);
        return subjectRepository.save(sujet);
    }

    @Transactional
    public void deleteSujet(Long sujetId) {
        subjectRepository.deleteById(sujetId);
    }
}