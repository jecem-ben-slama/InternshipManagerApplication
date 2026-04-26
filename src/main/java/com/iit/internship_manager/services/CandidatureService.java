package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.*;
import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.exceptions.*;
import com.iit.internship_manager.repositories.*;
import com.iit.internship_manager.web.dtos.CandidatureResponseDTO;
import com.iit.internship_manager.web.dtos.MessageRequest;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final MessageService messageService; // Inject the new service

    /**
     * Helper: Validates existence and ensures the authenticated teacher
     * is the assigned supervisor (enseignant_id) of the subject.
     */
    private Candidature getValidatedCandidatureForTeacher(Long id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", id));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // We use .getEnseignant() to check the supervisor assigned to the subject
        if (!candidature.getSujet().getEnseignant().getEmail().equals(email)) {
            throw new UnauthorizedActionException("Vous n'êtes pas le superviseur attitré de ce sujet.");
        }
        return candidature;
    }

    /**
     * Paginated retrieval for both Students and Teachers.
     */
    @Transactional(readOnly = true)
    public Page<CandidatureResponseDTO> getPagedCandidatures(DemandeStatus status, int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", null));

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Candidature> resultPage;

        if (user instanceof Etudiant) {
            resultPage = (status != null)
                    ? candidatureRepository.findByEtudiantIdAndStatut(user.getId(), status, pageable)
                    : candidatureRepository.findByEtudiantId(user.getId(), pageable);
        } else {
            // Teacher sees candidatures where they are the 'enseignant' (supervisor)
            resultPage = (status != null)
                    ? candidatureRepository.findBySujetEnseignantIdAndStatut(user.getId(), status, pageable)
                    : candidatureRepository.findBySujetEnseignantId(user.getId(), pageable);
        }

        return resultPage.map(CandidatureResponseDTO::fromEntity);
    }

    /**
     * Student applies for a subject.
     * Scenario 2: Subject must be AVAILABLE.
     */
    @Transactional
    public void postuler(Long sujetId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Etudiant student = (Etudiant) userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedActionException("Étudiant non trouvé"));

        Sujet sujet = subjectRepository.findById(sujetId)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", sujetId));

        if (sujet.getStatut() != SujetStatus.AVAILABLE) {
            throw new SujetIndisponibleException();
        }

        if (candidatureRepository.existsByEtudiantIdAndSujetId(student.getId(), sujetId)) {
            throw new DuplicateCandidatureException();
        }

        Candidature candidature = new Candidature();
        candidature.setEtudiant(student);
        candidature.setSujet(sujet);
        candidature.setStatut(DemandeStatus.PENDING);

        candidatureRepository.save(candidature);
    }

    /**
     * Teacher Action: ACCEPT
     * - Checks quota
     * - Updates status to ACCEPTED_BY_TEACHER
     * - Auto-rejects other pending applications for the same student
     */
    @Transactional
    public void accepterEtudiant(Long candidatureId) {
        Candidature selected = getValidatedCandidatureForTeacher(candidatureId);
        Enseignant teacher = selected.getSujet().getEnseignant(); // Corrected: Use Enseignant field

        if (teacher.getEncadrementsActuels() >= teacher.getQuotaAnnuel()) {
            throw new QuotaExceededException();
        }

        selected.setStatut(DemandeStatus.ACCEPTED_BY_TEACHER);
        teacher.setEncadrementsActuels(teacher.getEncadrementsActuels() + 1);

        // Professional logic: Reject other pending applications for this student
        List<Candidature> studentOtherApplications = candidatureRepository
                .findByEtudiantId(selected.getEtudiant().getId());

        studentOtherApplications.forEach(c -> {
            if (!c.getId().equals(candidatureId) &&
                    (c.getStatut() == DemandeStatus.PENDING || c.getStatut() == DemandeStatus.NEED_CLARIFICATION)) {
                c.setStatut(DemandeStatus.REJECTED_BY_TEACHER);
            }
        });

        userRepository.save(teacher);
        candidatureRepository.save(selected);
        candidatureRepository.saveAll(studentOtherApplications);
    }

    @Transactional
    public void refuserEtudiant(Long candidatureId) {
        Candidature candidature = getValidatedCandidatureForTeacher(candidatureId);
        candidature.setStatut(DemandeStatus.REJECTED_BY_TEACHER);
        candidatureRepository.save(candidature);
    }

    @Transactional
    public void demanderClarification(Long candidatureId, String justification) {
        Candidature candidature = getValidatedCandidatureForTeacher(candidatureId);

        // Update status
        candidature.setStatut(DemandeStatus.NEED_CLARIFICATION);
        candidatureRepository.save(candidature);

        // Use the message service to create the first message
        MessageRequest firstMsg = new MessageRequest();
        firstMsg.setContent(justification);
        messageService.sendMessage(candidatureId, firstMsg);
    }

    /**
     * Completes the internship record. Quota remains filled as work is done.
     */
    @Transactional
    public void terminerStage(Long candidatureId) {
        Candidature candidature = getValidatedCandidatureForTeacher(candidatureId);

        if (candidature.getStatut() != DemandeStatus.ACCEPTED_BY_TEACHER) {
            throw new IllegalCandidatureStateException("Seul un stage en cours peut être terminé.");
        }

        candidature.setStatut(DemandeStatus.FINISHED);
        candidatureRepository.save(candidature);
    }

    /**
     * Marks internship as abandoned.
     * Frees teacher quota and makes subject AVAILABLE again.
     */
    @Transactional
    public void abandonnerStage(Long candidatureId) {
        Candidature candidature = getValidatedCandidatureForTeacher(candidatureId);

        if (candidature.getStatut() != DemandeStatus.ACCEPTED_BY_TEACHER) {
            throw new IllegalCandidatureStateException("Seul un stage en cours peut être marqué comme abandonné.");
        }

        candidature.setStatut(DemandeStatus.ABANDONED);

        Enseignant teacher = candidature.getSujet().getEnseignant(); // Corrected: Use Enseignant field
        if (teacher.getEncadrementsActuels() > 0) {
            teacher.setEncadrementsActuels(teacher.getEncadrementsActuels() - 1);
        }

        Sujet sujet = candidature.getSujet();
        sujet.setStatut(SujetStatus.AVAILABLE);

        userRepository.save(teacher);
        subjectRepository.save(sujet);
        candidatureRepository.save(candidature);
    }

    @Transactional
    public void annulerCandidature(Long id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", id));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!candidature.getEtudiant().getEmail().equals(email)) {
            throw new UnauthorizedActionException("Accès non autorisé.");
        }

        if (candidature.getStatut() != DemandeStatus.PENDING) {
            throw new IllegalCandidatureStateException("Impossible d'annuler une candidature déjà traitée.");
        }

        candidatureRepository.delete(candidature);
    }
}