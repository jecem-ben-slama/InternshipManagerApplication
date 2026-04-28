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

    @Override
    @Transactional
    public void accepterEtudiant(Long candidatureId) {
        Candidature selected = getValidatedCandidatureForTeacher(candidatureId);
        Enseignant teacher = selected.getSujet().getEnseignant();

        // 1. State Guard: Only Pending or Clarification can be accepted
        if (selected.getStatut() != DemandeStatus.PENDING &&
                selected.getStatut() != DemandeStatus.NEED_CLARIFICATION) {
            throw new InvalidCandidatureStateException(
                    "Cette candidature a déjà été traitée et ne peut plus être acceptée.");
        }

        // 2. Race condition guard: re-verify all members are still free
        for (Etudiant member : selected.getGroupe().getMembres()) {
            if (affectationRepository.existsByGroupeMembresId(member.getId())) {
                selected.setStatut(DemandeStatus.REJECTED_BY_SYSTEM);
                candidatureRepository.save(selected);
                throw new BadRequestException("L'étudiant " + member.getNom() + " est déjà affecté ailleurs.");
            }
        }

        // 3. Quota Check
        if (teacher.getEncadrementsActuels() >= teacher.getQuotaAnnuel()) {
            throw new QuotaExceededException();
        }

        // 4. Update Status & Quota
        selected.setStatut(DemandeStatus.ACCEPTED_BY_TEACHER);
        teacher.setEncadrementsActuels(teacher.getEncadrementsActuels() + 1);

        // 5. Create official Affectation
        Affectation affectation = new Affectation();
        affectation.setGroupe(selected.getGroupe());
        affectation.setEncadrant(teacher);
        affectation.setSujet(selected.getSujet());
        affectation.setDateAffectation(LocalDateTime.now());
        affectation.setOriginalCandidature(selected);

        // 6. Mark subject as TAKEN

        affectationRepository.save(affectation);
        subjectRepository.save(selected.getSujet());
        userRepository.save(teacher);
        candidatureRepository.save(selected);

        // 7. Cleanup: Bulk reject competing applications
        rejectOtherApplicationsForGroup(selected);
    }

    @Override
    @Transactional
    public void postuler(Long sujetId, List<Long> partnerIds) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        if (!(currentUser instanceof Etudiant student)) {
            throw new UnauthorizedActionException("Seuls les étudiants peuvent postuler.");
        }

        Sujet sujet = subjectRepository.findById(sujetId)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", sujetId));

        if (sujet.getStatut() != SujetStatus.AVAILABLE && sujet.getStatut() != SujetStatus.PENDING) {
            throw new SujetIndisponibleException();
        }

        if (sujet.getProposant() != null && !sujet.getProposant().getId().equals(student.getId())) {
            throw new UnauthorizedActionException("Ce sujet est réservé à l'étudiant qui l'a proposé.");
        }

        Groupe group = groupeService.getOrCreateGroup(student, partnerIds);

        for (Etudiant member : group.getMembres()) {
            if (affectationRepository.existsByGroupeMembresId(member.getId())) {
                throw new BadRequestException("L'étudiant " + member.getNom() + " est déjà affecté.");
            }
        }

        if (candidatureRepository.existsByGroupeIdAndSujetId(group.getId(), sujetId)) {
            throw new DuplicateCandidatureException();
        }

        Candidature candidature = new Candidature();
        candidature.setGroupe(group);
        candidature.setSujet(sujet);
        candidature.setStatut(DemandeStatus.PENDING);

        candidatureRepository.save(candidature);
    }

    @Override
    @Transactional
    public void refuserEtudiant(Long candidatureId) {
        Candidature candidature = getValidatedCandidatureForTeacher(candidatureId);

        // NEW GUARD: Prevent refusing an already accepted student
        verifyCandidatureIsModifiable(candidature);

        candidature.setStatut(DemandeStatus.REJECTED_BY_TEACHER);
        candidatureRepository.save(candidature);
    }

    @Override
    @Transactional
    public void demanderClarification(Long candidatureId, String justification) {
        Candidature candidature = getValidatedCandidatureForTeacher(candidatureId);

        // NEW GUARD: Prevent clarifying an already accepted/finalized student
        verifyCandidatureIsModifiable(candidature);

        candidature.setStatut(DemandeStatus.NEED_CLARIFICATION);
        candidatureRepository.save(candidature);

        MessageRequest firstMsg = new MessageRequest();
        firstMsg.setContent(justification);
        messageService.sendMessage(candidatureId, firstMsg);
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
    public void annulerCandidature(Long id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", id));

        if (!isMember(candidature)) {
            throw new UnauthorizedActionException("Accès non autorisé.");
        }

        if (candidature.getStatut() != DemandeStatus.PENDING) {
            throw new InvalidCandidatureStateException("Impossible d'annuler une candidature déjà traitée.");
        }

        candidatureRepository.delete(candidature);
    }

    // -------------------------------------------------------------------------
    // Private Helpers
    // -------------------------------------------------------------------------

    private void verifyCandidatureIsModifiable(Candidature c) {
        if (c.getStatut() == DemandeStatus.ACCEPTED_BY_TEACHER ||
                c.getStatut() == DemandeStatus.VALIDATED_BY_RESPONSABLE) {
            throw new InvalidCandidatureStateException(
                    "Une candidature acceptée ne peut plus être modifiée par l'enseignant.");
        }
    }

    private boolean isMember(Candidature c) {
        Long currentUserId = securityContext.getCurrentUserId();
        return c.getGroupe().getMembres().stream()
                .anyMatch(m -> m.getId().equals(currentUserId));
    }

    private Candidature getValidatedCandidatureForTeacher(Long id) {
        Candidature c = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", id));

        if (!c.getSujet().getEnseignant().getId().equals(securityContext.getCurrentUserId())) {
            throw new UnauthorizedActionException("Vous n'êtes pas le superviseur de ce sujet.");
        }
        return c;
    }

    private void rejectOtherApplicationsForGroup(Candidature successfulApp) {
        List<Long> memberIds = successfulApp.getGroupe().getMembres()
                .stream()
                .map(Etudiant::getId)
                .toList();

        List<Candidature> toReject = candidatureRepository
                .findByGroupeMembresIdInAndStatutIn(
                        memberIds,
                        List.of(DemandeStatus.PENDING, DemandeStatus.NEED_CLARIFICATION))
                .stream()
                .filter(app -> !app.getId().equals(successfulApp.getId()))
                .toList();

        toReject.forEach(app -> app.setStatut(DemandeStatus.REJECTED_BY_SYSTEM));
        candidatureRepository.saveAll(toReject);
    }
}