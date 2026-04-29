package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.*;
import com.iit.internship_manager.domain.enums.SujetStatus;
import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
import com.iit.internship_manager.domain.exceptions.UnauthorizedActionException;
import com.iit.internship_manager.repositories.AcademicYearRepository;
import com.iit.internship_manager.repositories.AffectationRepository;
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
    private final ISecurityContext securityContext;
    private final CurrentYearProvider currentYearProvider; // For year logic
    private final AcademicYearRepository academicYearRepository; // For year logic

    @Override
    @Transactional(readOnly = true)
    public Page<AffectationResponseDTO> getMyAffectations(Pageable pageable) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        AcademicYear currentYear = currentYearProvider.getCurrent();

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
            // Students should only have ONE affectation per academic year
            List<Affectation> affectations = affectationRepository
                    .findByGroupeMembresIdAndAnneeUniversitaire(student.getId(), currentYear);
            return new PageImpl<>(
                    affectations.stream().map(AffectationResponseDTO::fromEntity).toList(),
                    pageable,
                    affectations.size());
        }

        throw new UnauthorizedActionException("Rôle non reconnu.");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AffectationResponseDTO> getAffectationsByYear(String yearId, Pageable pageable) {
        // 1. Fetch the specific academic year requested
        AcademicYear year = academicYearRepository.findById(yearId)
                .orElseThrow(() -> new ResourceNotFoundException("Année universitaire"+ yearId +" introuvable", null));

        // 2. Identify the current user and their department
        Utilisateur currentUser = securityContext.getCurrentUser();
        if (!(currentUser instanceof Enseignant teacher)) {
            throw new UnauthorizedActionException("Accès restreint aux enseignants pour consulter les archives.");
        }

        // 3. Query the repository for affectations in that year and department
        return affectationRepository.findByDepartmentAndAnneeUniversitaire(
                teacher.getDepartment(),
                year,
                pageable).map(AffectationResponseDTO::fromEntity);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<StudentWorkloadDTO> getWorkloadView(Pageable pageable) {
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
            throw new UnauthorizedActionException("Accès non autorisé.");
        }

        return affectationsPage.map(this::mapToWorkloadDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Etudiant> getUnassignedStudents(Pageable pageable) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        if (!securityContext.isResponsablePFE() || !(currentUser instanceof Enseignant resp)) {
            throw new UnauthorizedActionException("Accès réservé au responsable.");
        }

        // This query must filter students who DON'T have an affectation in the CURRENT
        // year
        return userRepository.findStudentsWithoutAffectationByDepartmentAndYear(resp.getDepartment(),
                currentYearProvider.getCurrent(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeacherWorkloadDTO> getTeachersWorkload(Pageable pageable) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        AcademicYear currentYear = currentYearProvider.getCurrent();

        if (!securityContext.isResponsablePFE() || !(currentUser instanceof Enseignant resp)) {
            throw new UnauthorizedActionException("Accès réservé au responsable.");
        }

        return userRepository.findAllEnseignantsByDepartment(resp.getDepartment(), pageable)
                .map(e -> {
                    // DYNAMIC COUNT: Instead of using e.getEncadrementsActuels(), we count actual
                    // DB records for this year
                    long currentCount = affectationRepository.countByEncadrantAndAnneeUniversitaire(e, currentYear);

                    double percentage = (e.getQuotaAnnuel() > 0)
                            ? ((double) currentCount / e.getQuotaAnnuel()) * 100
                            : 0;

                    return TeacherWorkloadDTO.builder()
                            .teacherId(e.getId())
                            .teacherName(e.getNom() + " " + e.getPrenom())
                            .anneeId(currentYear.getId()) // Matches the new field
                            .currentEncadrements((int) currentCount)
                            .maxQuota(e.getQuotaAnnuel())
                            .occupationPercentage(percentage)
                            .build();
                });
    }

    @Override
    @Transactional
    public void abortAffectation(Long affectationId) {
        Affectation affectation = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation introuvable", affectationId));

        checkResponsableAccess(affectation);

        // Reset subject status
        Sujet sujet = affectation.getSujet();
        sujet.setStatut(SujetStatus.AVAILABLE);

        affectationRepository.delete(affectation);
        // Note: We don't manually decrement teacher.encadrementsActuels anymore because
        // count is dynamic
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalAssignmentsCount() {
        Utilisateur currentUser = securityContext.getCurrentUser();
        AcademicYear currentYear = currentYearProvider.getCurrent();

        if (securityContext.isResponsablePFE() && currentUser instanceof Enseignant resp) {
            return affectationRepository.countByDepartmentAndAnneeUniversitaire(resp.getDepartment(), currentYear);
        }
        return affectationRepository.countByAnneeUniversitaire(currentYear);
    }

    // --- Helpers stay similar but verify department logic ---
    // completeProject remains the same as it updates status by ID
    @Override
    @Transactional
    public void completeProject(Long id) {
        Affectation affectation = affectationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation not found", id));
        checkResponsableAccess(affectation);
        affectation.setStatus("COMPLETED");
        affectationRepository.save(affectation);
    }

    private void checkResponsableAccess(Affectation affectation) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        if (securityContext.isResponsablePFE() && currentUser instanceof Enseignant resp) {
            if (!affectation.getEncadrant().getDepartment().equals(resp.getDepartment())) {
                throw new UnauthorizedActionException("Vous ne pouvez agir que sur votre département.");
            }
        }
    }

    private StudentWorkloadDTO mapToWorkloadDTO(Affectation affectation) {
        List<StudentWorkloadDTO.CoworkerDTO> members = affectation.getGroupe().getMembres().stream()
                .map(m -> new StudentWorkloadDTO.CoworkerDTO(m.getId(), m.getNom(), m.getEmail()))
                .toList();

        return StudentWorkloadDTO.builder()
                .affectationId(affectation.getId())
                .status(affectation.getStatus() != null ? affectation.getStatus().toString() : "UNKNOWN")
                .dateAffectation(affectation.getDateAffectation())
                .sujetId(affectation.getSujet().getId())
                .sujetTitre(affectation.getSujet().getTitre())
                .sujetDescription(affectation.getSujet().getDescription())
                .encadrantNom(affectation.getEncadrant().getNom())
                .encadrantEmail(affectation.getEncadrant().getEmail())
                .coworkers(members)
                .build();
    }
}