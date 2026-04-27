package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.*;
import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.exceptions.*;
import com.iit.internship_manager.repositories.*;
import com.iit.internship_manager.services.interfaces.*;
import com.iit.internship_manager.web.dtos.CandidatureResponseDTO;
import com.iit.internship_manager.web.dtos.MessageRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidatureServiceImpl implements ICandidatureService {

    private final CandidatureRepository candidatureRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final IMessageService messageService; 
    private final IGroupeService groupeService;   
    private final ISecurityContext securityContext;
    private final AffectationRepository affectationRepository;

    /**
     * Teacher Action: ACCEPT
     */
    @Override
    @Transactional
    public void accepterEtudiant(Long candidatureId) {
        Candidature selected = getValidatedCandidatureForTeacher(candidatureId);
        Enseignant teacher = selected.getSujet().getEnseignant();

        // 1. Logic Validation
        if (selected.getStatut() != DemandeStatus.PENDING &&
                selected.getStatut() != DemandeStatus.NEED_CLARIFICATION) {
            throw new IllegalStateException("Seules les candidatures en attente ou en clarification peuvent être acceptées.");
        }

        // 2. Business Rule: Quota Check
        if (teacher.getEncadrementsActuels() >= teacher.getQuotaAnnuel()) {
            throw new QuotaExceededException();
        }

        // 3. Update Status
        selected.setStatut(DemandeStatus.ACCEPTED_BY_TEACHER);
        teacher.setEncadrementsActuels(teacher.getEncadrementsActuels() + 1);

        // 4. Create official Affectation
        Affectation affectation = new Affectation();
        affectation.setGroupe(selected.getGroupe());
        affectation.setEncadrant(teacher);
        affectation.setSujet(selected.getSujet());
        affectation.setDateAffectation(LocalDateTime.now());
        affectation.setOriginalCandidature(selected);

        affectationRepository.save(affectation);

        // 5. Cleanup: Reject competing applications for these students
        rejectOtherApplicationsForGroup(selected);

        userRepository.save(teacher);
        candidatureRepository.save(selected);
    }

    /**
     * Student Action: APPLY
     */
    @Override
    @Transactional
    public void postuler(Long sujetId, List<Long> partnerIds) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        if (!(currentUser instanceof Etudiant student)) {
            throw new UnauthorizedActionException("Seuls les étudiants peuvent postuler à des sujets.");
        }

        Sujet sujet = subjectRepository.findById(sujetId)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", sujetId));

        if (sujet.getStatut() != SujetStatus.AVAILABLE) {
            throw new SujetIndisponibleException();
        }

        // Check if student is already affected to a validated internship
        boolean alreadyAffected = candidatureRepository.findByGroupeMembresId(student.getId()).stream()
                .anyMatch(c -> c.getStatut() == DemandeStatus.VALIDATED_BY_RESPONSABLE);

        if (alreadyAffected) {
            throw new BadRequestException("Vous êtes déjà affecté à un sujet.");
        }

        // DECOUPLED: Delegate group logic to the IGroupeService
        Groupe group = groupeService.getOrCreateGroup(student, partnerIds);

        Candidature candidature = new Candidature();
        candidature.setGroupe(group);
        candidature.setSujet(sujet);
        candidature.setStatut(DemandeStatus.PENDING);

        candidatureRepository.save(candidature);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CandidatureResponseDTO> getPagedCandidatures(DemandeStatus status, int page, int size) {
        Utilisateur user = securityContext.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        
        Page<Candidature> resultPage;
        if (user instanceof Etudiant) {
            resultPage = (status != null)
                    ? candidatureRepository.findByGroupeMembresIdAndStatut(user.getId(), status, pageable)
                    : candidatureRepository.findByGroupeMembresId(user.getId(), pageable);
        } else {
            resultPage = (status != null)
                    ? candidatureRepository.findBySujetEnseignantIdAndStatut(user.getId(), status, pageable)
                    : candidatureRepository.findBySujetEnseignantId(user.getId(), pageable);
        }

        return resultPage.map(CandidatureResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public void refuserEtudiant(Long candidatureId) {
        Candidature candidature = getValidatedCandidatureForTeacher(candidatureId);
        candidature.setStatut(DemandeStatus.REJECTED_BY_TEACHER);
        candidatureRepository.save(candidature);
    }

    @Override
    @Transactional
    public void demanderClarification(Long candidatureId, String justification) {
        Candidature candidature = getValidatedCandidatureForTeacher(candidatureId);
        candidature.setStatut(DemandeStatus.NEED_CLARIFICATION);
        candidatureRepository.save(candidature);

        // Uses IMessageService to start the discussion
        MessageRequest firstMsg = new MessageRequest();
        firstMsg.setContent(justification);
        messageService.sendMessage(candidatureId, firstMsg);
    }

    @Override
    @Transactional
    public void annulerCandidature(Long id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", id));

        Long currentUserId = securityContext.getCurrentUserId();
        boolean isMember = candidature.getGroupe().getMembres().stream()
                .anyMatch(m -> m.getId().equals(currentUserId));

        if (!isMember) throw new UnauthorizedActionException("Accès non autorisé.");
        
        if (candidature.getStatut() != DemandeStatus.PENDING) {
            throw new IllegalStateException("Impossible d'annuler une candidature déjà traitée.");
        }

        candidatureRepository.delete(candidature);
    }

    // --- Private Helpers ---

    private Candidature getValidatedCandidatureForTeacher(Long id) {
        Candidature c = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", id));

        if (!c.getSujet().getEnseignant().getId().equals(securityContext.getCurrentUserId())) {
            throw new UnauthorizedActionException("Vous n'êtes pas le superviseur de ce sujet.");
        }
        return c;
    }

    private void rejectOtherApplicationsForGroup(Candidature successfulApp) {
        successfulApp.getGroupe().getMembres().forEach(student -> {
            List<Candidature> others = candidatureRepository.findByGroupeMembresId(student.getId());
            others.stream()
                .filter(app -> !app.getId().equals(successfulApp.getId()))
                .filter(app -> app.getStatut() == DemandeStatus.PENDING || app.getStatut() == DemandeStatus.NEED_CLARIFICATION)
                .forEach(app -> {
                    app.setStatut(DemandeStatus.REJECTED_BY_SYSTEM);
                    candidatureRepository.save(app);
                });
        });
    }
}