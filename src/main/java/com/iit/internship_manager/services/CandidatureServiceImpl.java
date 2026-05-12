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
import org.thymeleaf.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidatureServiceImpl implements ICandidatureService {

    private static final Logger log = LoggerFactory.getLogger(CandidatureServiceImpl.class);

    private final CandidatureRepository candidatureRepository;
    private final SubjectRepository subjectRepository;
    private final IMessageService messageService;
    private final IGroupeService groupeService;
    private final ISecurityContext securityContext;
    private final AffectationRepository affectationRepository;
    private final GroupeRepository groupeRepository;
    private final CurrentYearProvider currentYearProvider;
    private final AcademicYearRepository academicYearRepository;
    private final EnseignantRepository enseignantRepository;
    private final IEmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public CandidatureResponseDTO findById(Long candidatureId) {
        Candidature candidature = candidatureRepository.findById(candidatureId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", candidatureId));

        Utilisateur currentUser = securityContext.getCurrentUser();
        boolean isTeacher = candidature.getSujet().getEnseignant().getId().equals(currentUser.getId());
        boolean isMember = candidature.getGroupe() != null
                && candidature.getGroupe().getMembres() != null
                && candidature.getGroupe().getMembres().stream()
                        .anyMatch(member -> member.getId().equals(currentUser.getId()));

        if (!isTeacher && !isMember && currentUser.getRole() != Role.ADMIN_IT) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Acces refuse a cette candidature.");
        }

        return CandidatureResponseDTO.fromEntity(candidature);
    }

    @Override
    @Transactional
    public void accepterEtudiant(Long candidatureId) {
        Candidature selected = getValidatedCandidatureForTeacher(candidatureId);
        Enseignant teacher = selected.getSujet().getEnseignant();
        Sujet sujet = selected.getSujet();
        AcademicYear currentYear = currentYearProvider.getCurrent();

        // 1. State Guard
        if (selected.getStatut() != DemandeStatus.PENDING &&
                selected.getStatut() != DemandeStatus.NEED_CLARIFICATION) {
            throw new DomainException(ErrorCode.BUSINESS_RULE_ERROR, "Cette candidature a déjà été traitée.");
        }

        // 2. Race condition guard: verify all members are still free for the current
        // year
        List<Long> memberIds = selected.getGroupe().getMembres().stream().map(Etudiant::getId).toList();
        if (affectationRepository.existsByGroupeMembresIdInAndAnneeUniversitaire(memberIds, currentYear)) {
            selected.setStatut(DemandeStatus.REJECTED_BY_SYSTEM);
            candidatureRepository.save(selected);
            throw new DomainException(ErrorCode.BUSINESS_RULE_ERROR,
                    "Un ou plusieurs membres du groupe sont déjà affectés pour cette année.");
        }

        // 3. Quota Check (year-aware)
        long currentCount = affectationRepository.countByEncadrantAndAnneeUniversitaire(teacher, currentYear);
        if (currentCount >= teacher.getQuotaAnnuel()) {
            throw new DomainException(ErrorCode.BUSINESS_RULE_ERROR, "Le quota annuel de l'enseignant a été dépassé.");
        }

        // 4. Update candidature status
        selected.setStatut(DemandeStatus.ACCEPTED_BY_TEACHER);
        sujet.setStatut(SujetStatus.TAKEN);

        // 6. Auto-lock remaining subjects if teacher hits quota
        if ((currentCount + 1) >= teacher.getQuotaAnnuel()) {
            subjectRepository.markAllSubjectsAsTakenForTeacher(teacher.getId());
        }

        // 7. Create official Affectation
        Groupe affectationGroup = ensureGroupAvailableForAffectation(selected.getGroupe());
        if (!affectationGroup.getId().equals(selected.getGroupe().getId())) {
            selected.setGroupe(affectationGroup);
            candidatureRepository.save(selected);
        }

        Affectation affectation = new Affectation();
        affectation.setGroupe(affectationGroup);
        affectation.setEncadrant(teacher);
        affectation.setSujet(sujet);
        affectation.setDateAffectation(LocalDateTime.now());
        affectation.setOriginalCandidature(selected);
        affectation.setAnneeUniversitaire(currentYear);

        affectationRepository.save(affectation);
        subjectRepository.save(sujet);
        candidatureRepository.save(selected);

        // 8. Notifications
        notifyResponsablePFE(affectation);
        notifyStudents(selected);

        // 9. Cleanup: reject other pending applications from the same group
        rejectOtherApplicationsForGroup(selected);
    }

    private void notifyResponsablePFE(Affectation aff) {
        try {
            List<Enseignant> responsables = enseignantRepository
                    .findResponsablesByDepartment(aff.getEncadrant().getDepartment());

            if (responsables.isEmpty()) {
                return;
            }

            Context context = new Context();
            context.setVariable("supervisorName",
                    aff.getEncadrant().getNom() + " " + aff.getEncadrant().getPrenom());
            context.setVariable("groupName", aff.getGroupe().getNom());
            context.setVariable("sujet", aff.getSujet().getTitre());

            responsables.forEach(responsable -> emailService.sendEmail(
                    responsable.getEmail(),
                    "New Affectation Created: " + aff.getSujet().getTitre(),
                    "new-affectation",
                    context,
                    null));
        } catch (Exception ex) {
            log.warn("Responsable notification skipped for affectation {}", aff.getId(), ex);
        }
    }

    private void notifyStudents(Candidature cand) {
        try {
            Context context = new Context();
            context.setVariable("sujet", cand.getSujet().getTitre());
            context.setVariable("teacherName",
                    cand.getSujet().getEnseignant().getNom() + " " + cand.getSujet().getEnseignant().getPrenom());

            cand.getGroupe().getMembres().forEach(student -> emailService.sendEmail(
                    student.getEmail(),
                    "Application Accepted!",
                    "candidature-accepted",
                    context,
                    null));
        } catch (Exception ex) {
            log.warn("Student notification skipped for candidature {}", cand.getId(), ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CandidatureResponseDTO> getCandidaturesByYear(String yearId, DemandeStatus status, int page, int size) {
        AcademicYear year = academicYearRepository.findById(yearId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Année universitaire " + yearId + " introuvable", null));

        Utilisateur currentUser = securityContext.getCurrentUser();
        if (!(currentUser instanceof Enseignant teacher)) {
            throw new DomainException(ErrorCode.FORBIDDEN,
                    "Seuls les enseignants peuvent consulter les archives des candidatures.");
        }

        Pageable pageable = PageRequest.of(page, size);
        return candidatureRepository
                .findByTeacherAndYearAndStatus(teacher.getId(), year, status, pageable)
                .map(CandidatureResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public void postuler(Long sujetId, List<Long> partnerIds) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        if (!(currentUser instanceof Etudiant student)) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Seuls les étudiants peuvent postuler.");
        }

        AcademicYear currentYear = currentYearProvider.getCurrent();
        Sujet sujet = subjectRepository.findById(sujetId)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", sujetId));

        if (!student.getDepartment().equals(sujet.getEnseignant().getDepartment())) {
            throw new DomainException(ErrorCode.FORBIDDEN,
                    "Vous ne pouvez postuler qu'aux sujets de votre département.");
        }

        if (sujet.getStatut() != SujetStatus.AVAILABLE && sujet.getStatut() != SujetStatus.PENDING) {
            throw new DomainException(ErrorCode.BUSINESS_RULE_ERROR,
                    "Ce sujet n'est plus disponible pour les candidatures.");
        }

        if (sujet.getProposant() != null && !sujet.getProposant().getId().equals(student.getId())) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Ce sujet est réservé.");
        }

        Groupe group = groupeService.getOrCreateGroup(student, partnerIds);
        List<Long> memberIds = group.getMembres().stream().map(Etudiant::getId).toList();

        if (affectationRepository.existsByGroupeMembresIdInAndAnneeUniversitaire(memberIds, currentYear)) {
            throw new DomainException(ErrorCode.BUSINESS_RULE_ERROR,
                    "Un membre est déjà affecté pour l'année " + currentYear.getId());
        }

        if (candidatureRepository.existsByGroupeIdAndSujetId(group.getId(), sujetId)) {
            throw new DomainException(ErrorCode.BUSINESS_RULE_ERROR,
                    "Une candidature existe déjà pour ce groupe et ce sujet.");
        }

        Candidature candidature = new Candidature();
        candidature.setGroupe(group);
        candidature.setSujet(sujet);
        candidature.setStatut(DemandeStatus.PENDING);
        candidature.setAnneeUniversitaire(currentYear);
        candidatureRepository.save(candidature);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CandidatureResponseDTO> getPagedCandidatures(DemandeStatus status, int page, int size) {
        Utilisateur user = securityContext.getCurrentUser();
        AcademicYear currentYear = currentYearProvider.getCurrent();
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Candidature> resultPage;
        if (user instanceof Etudiant) {
            resultPage = (status != null)
                    ? candidatureRepository.findByGroupeMembresIdAndStatutAndAnneeUniversitaire(
                            user.getId(), status, currentYear, pageable)
                    : candidatureRepository.findByGroupeMembresIdAndAnneeUniversitaire(
                            user.getId(), currentYear, pageable);
        } else {
            resultPage = candidatureRepository.findByTeacherAndYearAndStatus(
                    user.getId(), currentYear, status, pageable);
        }
        return resultPage.map(CandidatureResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public void refuserEtudiant(Long candidatureId) {
        Candidature candidature = getValidatedCandidatureForTeacher(candidatureId);
        verifyCandidatureIsModifiable(candidature);
        candidature.setStatut(DemandeStatus.REJECTED_BY_TEACHER);
        candidatureRepository.save(candidature);
    }

    @Override
    @Transactional
    public void demanderClarification(Long candidatureId, String justification) {
        Candidature candidature = getValidatedCandidatureForTeacher(candidatureId);
        verifyCandidatureIsModifiable(candidature);
        candidature.setStatut(DemandeStatus.NEED_CLARIFICATION);
        candidatureRepository.save(candidature);

        MessageRequest msg = new MessageRequest();
        msg.setContent(justification);
        messageService.sendMessage(candidatureId, msg);
    }

    @Override
    @Transactional
    public void annulerCandidature(Long id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", id));
        if (!isMember(candidature))
            throw new DomainException(ErrorCode.FORBIDDEN, "Accès non autorisé.");
        if (candidature.getStatut() != DemandeStatus.PENDING)
            throw new DomainException(ErrorCode.BUSINESS_RULE_ERROR, "Déjà traitée.");
        candidatureRepository.delete(candidature);
    }

    // --- Private Helpers ---

    private void verifyCandidatureIsModifiable(Candidature c) {
        if (c.getStatut() == DemandeStatus.ACCEPTED_BY_TEACHER ||
                c.getStatut() == DemandeStatus.VALIDATED_BY_RESPONSABLE) {
            throw new DomainException(ErrorCode.BUSINESS_RULE_ERROR,
                    "Impossible de modifier une candidature acceptée.");
        }
    }

    private boolean isMember(Candidature c) {
        return c.getGroupe().getMembres().stream()
                .anyMatch(m -> m.getId().equals(securityContext.getCurrentUserId()));
    }

    private Candidature getValidatedCandidatureForTeacher(Long id) {
        Candidature c = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature", id));
        if (!c.getSujet().getEnseignant().getId().equals(securityContext.getCurrentUserId())) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Non autorisé.");
        }
        return c;
    }

    private void rejectOtherApplicationsForGroup(Candidature successfulApp) {
        List<Long> memberIds = successfulApp.getGroupe().getMembres().stream().map(Etudiant::getId).toList();
        AcademicYear currentYear = currentYearProvider.getCurrent();

        List<Candidature> toReject = candidatureRepository
                .findByGroupeMembresIdInAndStatutInAndAnneeUniversitaire(
                        memberIds,
                        List.of(DemandeStatus.PENDING, DemandeStatus.NEED_CLARIFICATION),
                        currentYear)
                .stream()
                .filter(app -> !app.getId().equals(successfulApp.getId()))
                .toList();

        toReject.forEach(app -> app.setStatut(DemandeStatus.REJECTED_BY_SYSTEM));
        candidatureRepository.saveAll(toReject);
    }

    private Groupe ensureGroupAvailableForAffectation(Groupe group) {
        if (affectationRepository.findByGroupeId(group.getId()).isEmpty()) {
            return group;
        }

        Groupe clonedGroup = Groupe.builder()
                .nom(group.getNom())
                .membres(new java.util.ArrayList<>(group.getMembres()))
                .build();

        return groupeRepository.save(clonedGroup);
    }
}
