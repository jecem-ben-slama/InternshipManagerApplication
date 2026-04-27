package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.*;
import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
import com.iit.internship_manager.domain.exceptions.UnauthorizedActionException;
import com.iit.internship_manager.repositories.AffectationRepository;
import com.iit.internship_manager.repositories.EnseignantRepository;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.services.interfaces.IAffectationService;
import com.iit.internship_manager.services.interfaces.ISecurityContext;
import com.iit.internship_manager.web.dtos.AffectationResponseDTO;
import com.iit.internship_manager.web.dtos.StudentWorkloadDTO;
import com.iit.internship_manager.web.dtos.TeacherWorkloadDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
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
    private final EnseignantRepository enseignantRepository;
    private final ISecurityContext securityContext;

    @Override
    @Transactional(readOnly = true)
    public Page<AffectationResponseDTO> getMyAffectations(Pageable pageable) {
        Utilisateur currentUser = securityContext.getCurrentUser();

        if (securityContext.isResponsablePFE()) {
            return affectationRepository.findAll(pageable)
                    .map(AffectationResponseDTO::fromEntity);
        }

        if (currentUser instanceof Enseignant teacher) {
            return affectationRepository.findByEncadrantId(teacher.getId(), pageable)
                    .map(AffectationResponseDTO::fromEntity);
        }

        if (currentUser instanceof Etudiant student) {
            List<Affectation> affectations = affectationRepository.findByStudentId(student.getId());

            if (affectations.size() > 1) {
                throw new DataIntegrityViolationException(
                        "Conflict: Multiple project assignments detected for student ID: " + student.getId());
            }

            return new PageImpl<>(
                    affectations.stream().map(AffectationResponseDTO::fromEntity).toList(),
                    pageable,
                    affectations.size());
        }

        throw new UnauthorizedActionException("Rôle non reconnu.");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentWorkloadDTO> getWorkloadView(Pageable pageable) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        Page<Affectation> affectationsPage;

        if (securityContext.isResponsablePFE() && currentUser instanceof Enseignant teacher) {
            affectationsPage = affectationRepository.findByEncadrantId(teacher.getId(), pageable);
        } else if (currentUser instanceof Etudiant student) {
            // Students only have one (usually), but we return it as a Page of 1
            List<Affectation> list = affectationRepository.findByStudentId(student.getId());

            if (list.stream().filter(a -> "IN_PROGRESS".equals(a.getStatus())).count() > 1) {
                throw new DataIntegrityViolationException("Conflict: Multiple active projects detected.");
            }

            affectationsPage = new PageImpl<>(list, pageable, list.size());
        } else {
            throw new UnauthorizedActionException("Role not authorized for workload view.");
        }

        return affectationsPage.map(this::mapToWorkloadDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Etudiant> getUnassignedStudents(Pageable pageable) {
        if (!securityContext.isResponsablePFE()) {
            throw new UnauthorizedActionException("Accès réservé au responsable.");
        }
        return userRepository.findStudentsWithoutAffectation(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeacherWorkloadDTO> getTeachersWorkload(Pageable pageable) {
        if (!securityContext.isResponsablePFE()) {
            throw new UnauthorizedActionException("Accès réservé au responsable.");
        }

        // We use the repository to get a page of Enseignant directly
        return userRepository.findAllEnseignants(pageable)
                .map(e -> {
                    double percentage = (e.getQuotaAnnuel() > 0)
                            ? ((double) e.getEncadrementsActuels() / e.getQuotaAnnuel()) * 100
                            : 0;

                    return new TeacherWorkloadDTO(
                            e.getId(),
                            e.getNom() + " " + e.getPrenom(),
                            e.getEncadrementsActuels(),
                            e.getQuotaAnnuel(),
                            percentage);
                });
    }

    // --- Helper & State Change Methods ---

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

    @Transactional
    public void completeProject(Long id) {
        Affectation affectation = affectationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation not found", id));
        affectation.setStatus("COMPLETED");
        affectationRepository.save(affectation);
    }

    @Transactional
    public void abortAffectation(Long affectationId) {
        Affectation affectation = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new ResourceNotFoundException("Affectation introuvable", affectationId));

        Enseignant teacher = affectation.getEncadrant();
        if (teacher.getEncadrementsActuels() > 0) {
            teacher.setEncadrementsActuels(teacher.getEncadrementsActuels() - 1);
            enseignantRepository.save(teacher);
        }
        affectationRepository.delete(affectation);
    }

    @Override
    public long getTotalAssignmentsCount() {
        return affectationRepository.count();
    }
}