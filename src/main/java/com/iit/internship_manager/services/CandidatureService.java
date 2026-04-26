package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.*;
import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.exceptions.*;
import com.iit.internship_manager.repositories.*;
import com.iit.internship_manager.web.dtos.CandidatureResponseDTO;
import com.iit.internship_manager.web.dtos.MessageRequest;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final JpaMessageService messageService;
    private final GroupeRepository groupeRepository;
    private final AffectationRepository affectationRepository;

    /**
     * Teacher Action: ACCEPT
     * Updates status, creates Affectation, and rejects competing applications.
     */
    @Transactional
    public void accepterEtudiant(Long candidatureId) {
        Candidature selected = getValidatedCandidatureForTeacher(candidatureId);
        Enseignant teacher = selected.getSujet().getEnseignant();

        // 1. Status Validation (Allowing both PENDING and NEED_CLARIFICATION)
        if (selected.getStatut() != DemandeStatus.PENDING &&
                selected.getStatut() != DemandeStatus.NEED_CLARIFICATION) {
            throw new IllegalStateException(
                    "Seules les candidatures en attente ou en clarification peuvent être acceptées.");
        }

        // 2. Quota Check
        if (teacher.getEncadrementsActuels() >= teacher.getQuotaAnnuel()) {
            throw new QuotaExceededException();
        }

        // 3. Update Status
        selected.setStatut(DemandeStatus.ACCEPTED_BY_TEACHER); // Or Accepedbyteacher
        teacher.setEncadrementsActuels(teacher.getEncadrementsActuels() + 1);

        // 4. Create the Official Affectation with the Link back to Chat
        Affectation affectation = new Affectation();
        affectation.setGroupe(selected.getGroupe());
        affectation.setEncadrant(teacher);
        affectation.setSujet(selected.getSujet());
        affectation.setDateAffectation(LocalDateTime.now());

        // CRITICAL: Link the affectation to the candidature so the chat persists
        affectation.setOriginalCandidature(selected);

        affectationRepository.save(affectation);

        // 5. CLEANUP: Reject other applications for ALL members of this group
        rejectOtherApplicationsForGroup(selected);

        userRepository.save(teacher);
        candidatureRepository.save(selected);
    }
    private void rejectOtherApplicationsForGroup(Candidature successfulCandidature) {
        List<Long> memberIds = successfulCandidature.getGroupe().getMembres().stream()
                .map(Etudiant::getId)
                .toList();

        for (Long studentId : memberIds) {
            List<Candidature> otherApps = candidatureRepository.findByGroupeMembresId(studentId);
            for (Candidature app : otherApps) {
                if (!app.getId().equals(successfulCandidature.getId()) &&
                        (app.getStatut() == DemandeStatus.PENDING
                                || app.getStatut() == DemandeStatus.NEED_CLARIFICATION)) {

                    app.setStatut(DemandeStatus.REJECTED_BY_SYSTEM);
                    candidatureRepository.save(app);
                }
            }
        }
    }

    /**
     * Student applies for a subject (Solo or with partners).
     */
    @Transactional
    public void postuler(Long sujetId, List<Long> partnerIds) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Etudiant student = (Etudiant) userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedActionException("Étudiant non trouvé"));

        Sujet sujet = subjectRepository.findById(sujetId)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", sujetId));

        if (sujet.getStatut() != SujetStatus.AVAILABLE) {
            throw new SujetIndisponibleException();
        }

        // Check if student is already affected
        boolean alreadyAffected = candidatureRepository.findByGroupeMembresId(student.getId()).stream()
                .anyMatch(c -> c.getStatut() == DemandeStatus.VALIDATED_BY_RESPONSABLE);

        if (alreadyAffected) {
            throw new BadRequestException("Vous êtes déjà affecté à un sujet.");
        }

        Groupe group = findOrCreateGroup(student, partnerIds);

        Candidature candidature = new Candidature();
        candidature.setGroupe(group);
        candidature.setSujet(sujet);
        candidature.setStatut(DemandeStatus.PENDING);

        candidatureRepository.save(candidature);
    }

    /**
     * Finds existing group with exact same members or creates a new one.
     */
    private Groupe findOrCreateGroup(Etudiant creator, List<Long> partnerIds) {
        List<Etudiant> targetMembers = new ArrayList<>();
        targetMembers.add(creator);
        if (partnerIds != null && !partnerIds.isEmpty()) {
            userRepository.findAllById(partnerIds).forEach(u -> targetMembers.add((Etudiant) u));
        }

        List<Long> targetIds = targetMembers.stream().map(Etudiant::getId).sorted().collect(Collectors.toList());

        // Check for existing group to prevent join-table spam
        List<Groupe> existingGroups = groupeRepository.findByMembresId(creator.getId());
        for (Groupe g : existingGroups) {
            List<Long> currentIds = g.getMembres().stream().map(Etudiant::getId).sorted().collect(Collectors.toList());
            if (currentIds.equals(targetIds)) {
                return g;
            }
        }

        String groupName = "Binôme: " + creator.getNom();
        if (targetMembers.size() > 1)
            groupName += " & " + targetMembers.get(1).getNom();

        Groupe newGroup = Groupe.builder()
                .nom(groupName)
                .membres(targetMembers)
                .build();
        return groupeRepository.save(newGroup);
    }

    @Transactional(readOnly = true)
    public Page<CandidatureResponseDTO> getPagedCandidatures(DemandeStatus status, int page, int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", null));

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

    @Transactional
    public void refuserEtudiant(Long candidatureId) {
        Candidature candidature = getValidatedCandidatureForTeacher(candidatureId);
        candidature.setStatut(DemandeStatus.REJECTED_BY_TEACHER);
        candidatureRepository.save(candidature);
    }

    @Transactional
    public void demanderClarification(Long candidatureId, String justification) {
        Candidature candidature = getValidatedCandidatureForTeacher(candidatureId);
        candidature.setStatut(DemandeStatus.NEED_CLARIFICATION);
        candidatureRepository.save(candidature);

        MessageRequest firstMsg = new MessageRequest();
        firstMsg.setContent(justification);
        messageService.sendMessage(candidatureId, firstMsg);
    }

    @Transactional
    public void annulerCandidature(Long id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", id));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isMember = candidature.getGroupe().getMembres().stream()
                .anyMatch(m -> m.getEmail().equals(email));

        if (!isMember)
            throw new UnauthorizedActionException("Accès non autorisé.");
        if (candidature.getStatut() != DemandeStatus.PENDING) {
            throw new IllegalCandidatureStateException("Impossible d'annuler une candidature déjà traitée.");
        }

        candidatureRepository.delete(candidature);
    }

    private Candidature getValidatedCandidatureForTeacher(Long id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", id));
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!candidature.getSujet().getEnseignant().getEmail().equals(email)) {
            throw new UnauthorizedActionException("Vous n'êtes pas le superviseur attitré de ce sujet.");
        }
        return candidature;
    }
}