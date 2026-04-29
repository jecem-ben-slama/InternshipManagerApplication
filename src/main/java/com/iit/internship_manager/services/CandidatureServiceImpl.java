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
    private final IMessageService messageService;
    private final IGroupeService groupeService;
    private final ISecurityContext securityContext;
    private final AffectationRepository affectationRepository;
    private final CurrentYearProvider currentYearProvider; // Added for Year Logic
    private final AcademicYearRepository academicYearRepository; // Added for Year Logic

    @Override
    @Transactional
    public void accepterEtudiant(Long candidatureId) {
        Candidature selected = getValidatedCandidatureForTeacher(candidatureId);
        Enseignant teacher = selected.getSujet().getEnseignant();
        Sujet sujet = selected.getSujet();
        AcademicYear currentYear = currentYearProvider.getCurrent(); // Get active year

        // 1. State Guard
        if (selected.getStatut() != DemandeStatus.PENDING &&
                selected.getStatut() != DemandeStatus.NEED_CLARIFICATION) {
            throw new InvalidCandidatureStateException("Cette candidature a déjà été traitée.");
        }

        // 2. Race condition guard: verify all members are still free for the CURRENT
        // year
        List<Long> memberIds = selected.getGroupe().getMembres().stream().map(Etudiant::getId).toList();
        if (affectationRepository.existsByGroupeMembresIdInAndAnneeUniversitaire(memberIds, currentYear)) {
            selected.setStatut(DemandeStatus.REJECTED_BY_SYSTEM);
            candidatureRepository.save(selected);
            throw new BadRequestException("Un ou plusieurs membres du groupe sont déjà affectés pour cette année.");
        }

        // 3. Quota Check (Needs to be year-aware)
        // We calculate current encadrements based on the active year only
        long currentCount = affectationRepository.countByEncadrantAndAnneeUniversitaire(teacher, currentYear);
        if (currentCount >= teacher.getQuotaAnnuel()) {
            throw new QuotaExceededException();
        }

        // 4. Update Status
        selected.setStatut(DemandeStatus.ACCEPTED_BY_TEACHER);

        // 5. Update Subject Status
        sujet.setStatut(SujetStatus.TAKEN);

        // --- NEW: YEAR-AWARE QUOTA AUTO-LOCK ---
        // If the teacher hits their limit for the active year, hide other current
        // available subjects
        if ((currentCount + 1) >= teacher.getQuotaAnnuel()) {
            subjectRepository.markAllSubjectsAsTakenForTeacher(teacher.getId());
        }

        // 6. Create official Affectation
        Affectation affectation = new Affectation();
        affectation.setGroupe(selected.getGroupe());
        affectation.setEncadrant(teacher);
        affectation.setSujet(sujet);
        affectation.setDateAffectation(LocalDateTime.now());
        affectation.setOriginalCandidature(selected);
        affectation.setAnneeUniversitaire(currentYear); // Stamp with active year

        // Persistent saves
        affectationRepository.save(affectation);
        subjectRepository.save(sujet);
        candidatureRepository.save(selected);

        // 7. Cleanup
        rejectOtherApplicationsForGroup(selected);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CandidatureResponseDTO> getCandidaturesByYear(String yearId, DemandeStatus status, int page, int size) {
        // 1. Fetch the requested Academic Year
        AcademicYear year = academicYearRepository.findById(yearId)
                .orElseThrow(() -> new ResourceNotFoundException("Année universitaire " + yearId + " introuvable", null));

        // 2. Identify the current user (Teacher)
        Utilisateur currentUser = securityContext.getCurrentUser();

        // 3. Security Check: Only teachers and admins should access archives here
        if (!(currentUser instanceof Enseignant teacher)) {
            throw new UnauthorizedActionException(
                    "Seuls les enseignants peuvent consulter les archives des candidatures.");
        }

        Pageable pageable = PageRequest.of(page, size);

        // 4. Query the repository
        return candidatureRepository.findByYearAndTeacher(year, status, teacher.getId(), pageable)
                .map(CandidatureResponseDTO::fromEntity);
    }
    @Override
    @Transactional
    public void postuler(Long sujetId, List<Long> partnerIds) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        if (!(currentUser instanceof Etudiant student)) {
            throw new UnauthorizedActionException("Seuls les étudiants peuvent postuler.");
        }

        AcademicYear currentYear = currentYearProvider.getCurrent();
        Sujet sujet = subjectRepository.findById(sujetId)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", sujetId));

        // Department Guard
        if (!student.getDepartment().equals(sujet.getEnseignant().getDepartment())) {
            throw new UnauthorizedActionException("Vous ne pouvez postuler qu'aux sujets de votre département.");
        }

        // Availability Check
        if (sujet.getStatut() != SujetStatus.AVAILABLE && sujet.getStatut() != SujetStatus.PENDING) {
            throw new SujetIndisponibleException();
        }

        // Reserved subject check
        if (sujet.getProposant() != null && !sujet.getProposant().getId().equals(student.getId())) {
            throw new UnauthorizedActionException("Ce sujet est réservé.");
        }

        Groupe group = groupeService.getOrCreateGroup(student, partnerIds);
        List<Long> memberIds = group.getMembres().stream().map(Etudiant::getId).toList();

        // Updated check: is anyone affected in the CURRENT year?
        if (affectationRepository.existsByGroupeMembresIdInAndAnneeUniversitaire(memberIds, currentYear)) {
            throw new BadRequestException("Un membre est déjà affecté pour l'année " + currentYear.getId());
        }

        if (candidatureRepository.existsByGroupeIdAndSujetId(group.getId(), sujetId)) {
            throw new DuplicateCandidatureException();
        }

        Candidature candidature = new Candidature();
        candidature.setGroupe(group);
        candidature.setSujet(sujet);
        candidature.setStatut(DemandeStatus.PENDING);
        candidature.setAnneeUniversitaire(currentYear); // Stamp the application with current year
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
            // Updated: filter by current year so students don't see 2025's applications in
            // 2026 by default
            resultPage = (status != null)
                    ? candidatureRepository.findByGroupeMembresIdAndStatutAndAnneeUniversitaire(user.getId(), status,
                            currentYear, pageable)
                    : candidatureRepository.findByGroupeMembresIdAndAnneeUniversitaire(user.getId(), currentYear,
                            pageable);
        } else {
            // Updated: filter by current year for teachers
            resultPage = (status != null)
                    ? candidatureRepository.findBySujetEnseignantIdAndStatutAndAnneeUniversitaire(user.getId(), status,
                            currentYear, pageable)
                    : candidatureRepository.findBySujetEnseignantIdAndAnneeUniversitaire(user.getId(), currentYear,
                            pageable);
        }
        return resultPage.map(CandidatureResponseDTO::fromEntity);
    }

    // ... (refuserEtudiant, demanderClarification, annulerCandidature stay largely
    // the same as they use ID)

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
            throw new UnauthorizedActionException("Accès non autorisé.");
        if (candidature.getStatut() != DemandeStatus.PENDING)
            throw new InvalidCandidatureStateException("Déjà traitée.");
        candidatureRepository.delete(candidature);
    }

    private void verifyCandidatureIsModifiable(Candidature c) {
        if (c.getStatut() == DemandeStatus.ACCEPTED_BY_TEACHER ||
                c.getStatut() == DemandeStatus.VALIDATED_BY_RESPONSABLE) {
            throw new InvalidCandidatureStateException("Impossible de modifier une candidature acceptée.");
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
            throw new UnauthorizedActionException("Non autorisé.");
        }
        return c;
    }

    private void rejectOtherApplicationsForGroup(Candidature successfulApp) {
        List<Long> memberIds = successfulApp.getGroupe().getMembres().stream().map(Etudiant::getId).toList();
        AcademicYear currentYear = currentYearProvider.getCurrent();

        // Only reject other applications in the CURRENT year
        List<Candidature> toReject = candidatureRepository
                .findByGroupeMembresIdInAndStatutInAndAnneeUniversitaire(memberIds,
                        List.of(DemandeStatus.PENDING, DemandeStatus.NEED_CLARIFICATION),
                        currentYear)
                .stream().filter(app -> !app.getId().equals(successfulApp.getId())).toList();

        toReject.forEach(app -> app.setStatut(DemandeStatus.REJECTED_BY_SYSTEM));
        candidatureRepository.saveAll(toReject);
    }
}