package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.*;
import com.iit.internship_manager.domain.enums.DemandeStatus;
import com.iit.internship_manager.domain.enums.DepartmentType;
import com.iit.internship_manager.domain.enums.ErrorCode;
import com.iit.internship_manager.domain.enums.SujetStatus;
import com.iit.internship_manager.domain.exceptions.DomainException;
import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
import com.iit.internship_manager.repositories.AcademicYearRepository;
import com.iit.internship_manager.repositories.AffectationRepository;
import com.iit.internship_manager.repositories.CandidatureRepository;
import com.iit.internship_manager.repositories.SubjectRepository;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.services.interfaces.IAffectationService;
import com.iit.internship_manager.services.interfaces.ISecurityContext;
import com.iit.internship_manager.web.dtos.AffectationResponseDTO;
import com.iit.internship_manager.web.dtos.StudentWorkloadDTO;
import com.iit.internship_manager.web.dtos.TeacherWorkloadDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AffectationServiceImpl implements IAffectationService {

    private final AffectationRepository affectationRepository;
    private final UserRepository userRepository;
    private final SubjectRepository sujetRepository;
    private final CandidatureRepository candidatureRepository;
    private final ISecurityContext securityContext;
    private final CurrentYearProvider currentYearProvider;
    private final AcademicYearRepository academicYearRepository;

    @Override
    @Transactional(readOnly = true)
    public AffectationResponseDTO getById(Long affectationId) {
        Affectation affectation = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation introuvable", affectationId));

        Utilisateur currentUser = securityContext.getCurrentUser();

        if (securityContext.isAdmin()) {
            return AffectationResponseDTO.fromEntity(affectation);
        }

        if (currentUser instanceof Enseignant teacher) {
            boolean isOwner = affectation.getEncadrant().getId().equals(teacher.getId());
            boolean isResponsableOfDept = securityContext.isResponsablePFE()
                    && affectation.getEncadrant().getDepartment().equals(teacher.getDepartment());

            if (isOwner || isResponsableOfDept) {
                return AffectationResponseDTO.fromEntity(affectation);
            }
        }

        if (currentUser instanceof Etudiant student) {
            boolean isMember = affectation.getGroupe() != null
                    && affectation.getGroupe().getMembres() != null
                    && affectation.getGroupe().getMembres().stream()
                            .anyMatch(member -> member.getId().equals(student.getId()));

            if (isMember) {
                return AffectationResponseDTO.fromEntity(affectation);
            }
        }

        throw new DomainException(ErrorCode.FORBIDDEN, "Acces refuse a cette affectation.");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AffectationResponseDTO> getMyAffectations(Pageable pageable) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        AcademicYear currentYear = currentYearProvider.getCurrent();
        if (securityContext.isAdmin()) {
            return affectationRepository.findByAnneeUniversitaire(currentYear, pageable)
                    .map(AffectationResponseDTO::fromEntity);
        }

        if (securityContext.isResponsablePFE() && currentUser instanceof Enseignant resp) {
            return affectationRepository
                    .findByDepartmentAndAnneeUniversitaire(resp.getDepartment(), currentYear, pageable)
                    .map(AffectationResponseDTO::fromEntity);
        }

        if (currentUser instanceof Enseignant teacher) {
            return affectationRepository.findByEncadrantIdAndAnneeUniversitaire(teacher.getId(), currentYear, pageable)
                    .map(AffectationResponseDTO::fromEntity);
        }

        if (currentUser instanceof Etudiant student) {
            List<Affectation> affectations = affectationRepository
                    .findByGroupeMembresIdAndAnneeUniversitaire(student.getId(), currentYear);
            return new PageImpl<>(
                    affectations.stream().map(AffectationResponseDTO::fromEntity).toList(),
                    pageable,
                    affectations.size());
        }

        throw new DomainException(ErrorCode.FORBIDDEN, "Rôle non reconnu.");
    }

   @Override
@Transactional(readOnly = true)
public Page<AffectationResponseDTO> getAffectationsByYear(String yearId, Pageable pageable) {
    AcademicYear year = academicYearRepository.findById(yearId)
            .orElseThrow(() -> new ResourceNotFoundException("Annee universitaire " + yearId + " introuvable", null));

    if (securityContext.isAdmin()) {
        return affectationRepository.findByAnneeUniversitaire(year, pageable)
                .map(AffectationResponseDTO::fromEntity);
    }

    Utilisateur currentUser = securityContext.getCurrentUser();
    
    // Changed type from String to DepartmentType to match your domain model
    DepartmentType dept = null;

    if (currentUser instanceof Enseignant e) {
        dept = e.getDepartment();
    } else if (currentUser instanceof Etudiant s) {
        dept = s.getDepartment();
    }

    if (dept == null) {
        throw new DomainException(ErrorCode.FORBIDDEN, "Accès restreint : département introuvable.");
    }

    // Now 'dept' matches the expected DepartmentType parameter in the repository
    return affectationRepository.findByDepartmentAndAnneeUniversitaire(dept, year, pageable)
            .map(AffectationResponseDTO::fromEntity);
}
    @Override
    @Transactional(readOnly = true)
    public Page<StudentWorkloadDTO> getWorkloadView(Pageable pageable) {
        if (securityContext.isAdmin())
            throw new DomainException(ErrorCode.FORBIDDEN, "Accès refusé.");

        Utilisateur currentUser = securityContext.getCurrentUser();
        AcademicYear currentYear = currentYearProvider.getCurrent();
        Page<Affectation> affectationsPage;

        if (securityContext.isResponsablePFE() && currentUser instanceof Enseignant resp) {
            affectationsPage = affectationRepository.findByDepartmentAndAnneeUniversitaire(resp.getDepartment(),
                    currentYear, pageable);
        } else if (currentUser instanceof Enseignant teacher) {
            affectationsPage = affectationRepository.findByEncadrantIdAndAnneeUniversitaire(teacher.getId(),
                    currentYear, pageable);
        } else if (currentUser instanceof Etudiant student) {
            List<Affectation> list = affectationRepository.findByGroupeMembresIdAndAnneeUniversitaire(student.getId(),
                    currentYear);
            affectationsPage = new PageImpl<>(list, pageable, list.size());
        } else {
            throw new DomainException(ErrorCode.FORBIDDEN, "Accès non autorisé.");
        }

        return affectationsPage.map(this::mapToWorkloadDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeacherWorkloadDTO> getTeachersWorkload(Pageable pageable) {
        if (securityContext.isAdmin())
            throw new DomainException(ErrorCode.FORBIDDEN, "Accès refusé.");

        Utilisateur currentUser = securityContext.getCurrentUser();
        AcademicYear currentYear = currentYearProvider.getCurrent();

        // Responsable sees everyone in dept
        if (securityContext.isResponsablePFE() && currentUser instanceof Enseignant resp) {
            return userRepository.findAllEnseignantsByDepartment(resp.getDepartment(), pageable)
                    .map(e -> buildTeacherWorkload(e, currentYear));
        }

        // Regular teacher sees only their own quota
        if (currentUser instanceof Enseignant teacher) {
            return new PageImpl<>(List.of(buildTeacherWorkload(teacher, currentYear)));
        }

        throw new DomainException(ErrorCode.FORBIDDEN, "Accès réservé aux enseignants.");
    }

    @Override
    @Transactional
    public void abortAffectation(Long affectationId) {
        if (securityContext.isAdmin())
            throw new DomainException(ErrorCode.FORBIDDEN, "Accès refusé.");

        Affectation affectation = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation introuvable", affectationId));

        checkManagementAccess(affectation);

        if ("COMPLETED".equalsIgnoreCase(affectation.getStatus())) {
            throw new DomainException(ErrorCode.CONFLICT, "Une affectation terminee ne peut plus etre annulee.");
        }

        AcademicYear currentYear = affectation.getAnneeUniversitaire();
        Sujet currentSujet = affectation.getSujet();

        currentSujet.setStatut(SujetStatus.AVAILABLE);
        sujetRepository.save(currentSujet);

        affectationRepository.delete(affectation);
        affectationRepository.flush();

        // Refresh system logic for candidatures...
        reviveCandidatures(currentSujet, affectation.getOriginalCandidature(), currentYear);
    }

    @Override
    @Transactional
    public void completeProject(Long id) {
        if (securityContext.isAdmin())
            throw new DomainException(ErrorCode.FORBIDDEN, "Accès refusé.");

        Affectation affectation = affectationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation not found", id));

        checkManagementAccess(affectation);

        if ("COMPLETED".equalsIgnoreCase(affectation.getStatus())) {
            throw new DomainException(ErrorCode.CONFLICT, "Cette affectation est deja terminee.");
        }

        affectation.setStatus("COMPLETED");
        affectationRepository.save(affectation);
    }

    private void checkManagementAccess(Affectation affectation) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        if (!(currentUser instanceof Enseignant teacher)) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Action réservée aux enseignants.");
        }

        // Rule: Can manage if it's YOUR project OR you are the Responsable of the dept
        boolean isOwner = affectation.getEncadrant().getId().equals(teacher.getId());
        boolean isResponsableOfDept = securityContext.isResponsablePFE() &&
                affectation.getEncadrant().getDepartment().equals(teacher.getDepartment());

        if (!isOwner && !isResponsableOfDept) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Vous n'avez pas les droits sur cette affectation.");
        }
    }

    private TeacherWorkloadDTO buildTeacherWorkload(Enseignant e, AcademicYear year) {
        long currentCount = affectationRepository.countByEncadrantAndAnneeUniversitaire(e, year);
        double percentage = (e.getQuotaAnnuel() > 0) ? ((double) currentCount / e.getQuotaAnnuel()) * 100 : 0;

        return TeacherWorkloadDTO.builder()
                .teacherId(e.getId())
                .teacherName(e.getNom() + " " + e.getPrenom())
                .anneeId(year.getId())
                .currentEncadrements((int) currentCount)
                .maxQuota(e.getQuotaAnnuel())
                .occupationPercentage(percentage)
                .build();
    }

    private void reviveCandidatures(Sujet sujet, Candidature original, AcademicYear year) {
        List<Candidature> toRevive = candidatureRepository.findBySujetAndStatut(sujet,
                DemandeStatus.REJECTED_BY_SYSTEM);
        if (original != null)
            toRevive.add(original);

        for (Candidature cand : toRevive) {
            boolean isAnyMemberAssigned = cand.getGroupe().getMembres().stream()
                    .anyMatch(m -> affectationRepository.existsByEtudiantAndYear(m, year));
            if (!isAnyMemberAssigned) {
                cand.setStatut(DemandeStatus.PENDING);
            }
        }
        candidatureRepository.saveAll(toRevive);
    }

    private StudentWorkloadDTO mapToWorkloadDTO(Affectation affectation) {
        List<StudentWorkloadDTO.CoworkerDTO> members = affectation.getGroupe().getMembres().stream()
                .map(m -> new StudentWorkloadDTO.CoworkerDTO(m.getId(), m.getNom(), m.getEmail()))
                .toList();

        return StudentWorkloadDTO.builder()
                .affectationId(affectation.getId())
                .status(affectation.getStatus() != null ? affectation.getStatus() : "UNKNOWN")
                .dateAffectation(affectation.getDateAffectation())
                .sujetId(affectation.getSujet().getId())
                .sujetTitre(affectation.getSujet().getTitre())
                .sujetDescription(affectation.getSujet().getDescription())
                .encadrantNom(affectation.getEncadrant().getNom())
                .encadrantEmail(affectation.getEncadrant().getEmail())
                .coworkers(members)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Etudiant> getUnassignedStudents(Pageable pageable) {
        if (securityContext.isAdmin()) {
            return userRepository.findStudentsWithoutAffectation(pageable);
        }

        Utilisateur currentUser = securityContext.getCurrentUser();
        if (!securityContext.isResponsablePFE() || !(currentUser instanceof Enseignant resp)) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Acces reserve au responsable.");
        }
        return userRepository.findStudentsWithoutAffectationByDepartmentAndYear(resp.getDepartment(),
                currentYearProvider.getCurrent(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalAssignmentsCount() {
        if (securityContext.isAdmin())
            return 0;
        Utilisateur currentUser = securityContext.getCurrentUser();
        AcademicYear currentYear = currentYearProvider.getCurrent();
        if (securityContext.isResponsablePFE() && currentUser instanceof Enseignant resp) {
            return affectationRepository.countByDepartmentAndAnneeUniversitaire(resp.getDepartment(), currentYear);
        }
        return affectationRepository.countByAnneeUniversitaire(currentYear);
    }
}
