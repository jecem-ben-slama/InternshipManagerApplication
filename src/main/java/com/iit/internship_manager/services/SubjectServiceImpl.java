package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.*;
import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.exceptions.*;
import com.iit.internship_manager.repositories.*;
import com.iit.internship_manager.services.interfaces.*;
import com.iit.internship_manager.web.dtos.SujetRequest;
import com.iit.internship_manager.web.dtos.SujetResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements ISubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final CandidatureRepository candidatureRepository;
    private final AffectationRepository affectationRepository;
    private final IGroupeService groupeService;
    private final ISecurityContext securityContext;

    @Override
    @Transactional(readOnly = true)
    public Page<SujetResponseDTO> findAll(Pageable pageable) {
        // Uses JOIN FETCH internally in repository to avoid N+1
        return subjectRepository.findAll(pageable)
                .map(SujetResponseDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public SujetResponseDTO findById(Long id) {
        return subjectRepository.findByIdWithDetails(id)
                .map(SujetResponseDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SujetResponseDTO> getSubjectsByStatus(SujetStatus status, Pageable pageable) {
        return subjectRepository.findByStatut(status, pageable)
                .map(SujetResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public SujetResponseDTO updateSujet(Long id, SujetRequest dto) {
        Sujet sujet = subjectRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", id));

        // 1. Status Guard: Lock if already taken
        if (sujet.getStatut() == SujetStatus.TAKEN) {
            throw new UnauthorizedActionException("Ce sujet est déjà assigné et ne peut plus être modifié.");
        }

        // 2. Ownership Check (Null-safe)
        String currentUserEmail = securityContext.getCurrentUserEmail();
        boolean isTeacherOwner = sujet.getEnseignant() != null
                && sujet.getEnseignant().getEmail().equals(currentUserEmail);
        boolean isStudentOwner = sujet.getProposant() != null
                && sujet.getProposant().getEmail().equals(currentUserEmail);

        if (!isTeacherOwner && !isStudentOwner && !securityContext.hasRole("ADMIN_IT")) {
            throw new UnauthorizedActionException("Vous n'êtes pas autorisé à modifier ce sujet.");
        }

        mapCommonFields(sujet, dto);
        return SujetResponseDTO.fromEntity(subjectRepository.save(sujet));
    }

    @Override
    @Transactional
    public void deleteSujet(Long id) {
        Sujet sujet = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", id));

        // 1. Status Guard
        if (sujet.getStatut() == SujetStatus.TAKEN) {
            throw new UnauthorizedActionException("Impossible de supprimer un sujet déjà assigné.");
        }

        // 2. Relationship Guard
        if (candidatureRepository.existsBySujetId(id)) {
            throw new UnauthorizedActionException("Ce sujet a des candidatures actives et ne peut être supprimé.");
        }

        subjectRepository.delete(sujet);
    }

    @Override
    @Transactional
    public SujetResponseDTO teacherProposeSujet(SujetRequest dto) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        if (!(currentUser instanceof Enseignant teacher)) {
            throw new UnauthorizedActionException("Seuls les enseignants peuvent proposer des sujets.");
        }

        Sujet sujet = new Sujet();
        mapCommonFields(sujet, dto);
        sujet.setEnseignant(teacher);
        sujet.setStatut(SujetStatus.AVAILABLE);

        return SujetResponseDTO.fromEntity(subjectRepository.save(sujet));
    }

    @Override
    @Transactional
    public SujetResponseDTO studentProposeSujet(Long teacherId, SujetRequest dto) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        if (!(currentUser instanceof Etudiant student)) {
            throw new UnauthorizedActionException("Seuls les étudiants peuvent proposer des sujets.");
        }

        // 1. Availability verification for ALL group members
        validateMembersAvailability(student, dto.getPartnerIds());

        Enseignant teacher = userRepository.findEnseignantById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant", teacherId));

        Groupe group = groupeService.getOrCreateGroup(student, dto.getPartnerIds());

        // 2. Create Subject
        Sujet sujet = new Sujet();
        mapCommonFields(sujet, dto);
        sujet.setEnseignant(teacher);
        sujet.setProposant(student);
        sujet.setStatut(SujetStatus.PROPOSED_BY_STUDENT);

        Sujet savedSujet = subjectRepository.save(sujet);

        // 3. Create Automatic Candidature
        createAutomaticCandidature(group, savedSujet);

        return SujetResponseDTO.fromEntity(savedSujet);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SujetResponseDTO> getSubjectsByCurrentTeacher(Pageable pageable) {
        return subjectRepository.findByEnseignantId(securityContext.getCurrentUserId(), pageable)
                .map(SujetResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public SujetResponseDTO updateSujetStatus(Long id, SujetStatus status) {
        Utilisateur user = securityContext.getCurrentUser();
        if (!(user instanceof Enseignant e) || !e.isResponsablePFE()) {
            throw new UnauthorizedActionException("Action réservée au Responsable PFE.");
        }

        Sujet sujet = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", id));

        sujet.setStatut(status);
        return SujetResponseDTO.fromEntity(subjectRepository.save(sujet));
    }

    // --- PRIVATE HELPERS ---

    private void validateMembersAvailability(Etudiant creator, List<Long> partnerIds) {
        List<Long> allMemberIds = new ArrayList<>();
        allMemberIds.add(creator.getId());
        if (partnerIds != null)
            allMemberIds.addAll(partnerIds);

        // 1. Check official Affectations (Ground Truth)
        boolean anyMemberAffected = affectationRepository.existsByGroupeMembresIdIn(allMemberIds);
        if (anyMemberAffected) {
            throw new UnauthorizedActionException("Un ou plusieurs membres du groupe sont déjà affectés.");
        }

        // 2. Check for PENDING/ACCEPTED candidates to prevent "ghost" double-booking
        for (Long studentId : allMemberIds) {
            boolean hasActiveApplication = candidatureRepository.findByGroupeMembresId(studentId).stream()
                    .anyMatch(c -> c.getStatut() == DemandeStatus.VALIDATED_BY_RESPONSABLE
                            || c.getStatut() == DemandeStatus.ACCEPTED_BY_TEACHER);

            if (hasActiveApplication) {
                throw new UnauthorizedActionException(
                        "L'étudiant ID " + studentId + " a déjà un sujet en cours de validation.");
            }
        }
    }

    private void createAutomaticCandidature(Groupe group, Sujet sujet) {
        Candidature candidature = new Candidature();
        candidature.setGroupe(group);
        candidature.setSujet(sujet);
        candidature.setStatut(DemandeStatus.PENDING);
        candidatureRepository.save(candidature);
    }

    private void mapCommonFields(Sujet sujet, SujetRequest dto) {
        sujet.setTitre(dto.getTitre());
        sujet.setDescription(dto.getDescription());
        sujet.setTechnologies(dto.getTechnologies());
        sujet.setType(dto.getType());
    }
}