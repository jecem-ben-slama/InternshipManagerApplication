package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.*;
import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.exceptions.*;
import com.iit.internship_manager.repositories.*;
import com.iit.internship_manager.services.interfaces.ISecurityContext;
import com.iit.internship_manager.services.interfaces.ISubjectService;
import com.iit.internship_manager.services.interfaces.IGroupeService;
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
    private final IGroupeService groupeService;
    private final ISecurityContext securityContext;

    // --- RESTORED MISSING METHODS ---

    @Transactional(readOnly = true)
    public Page<SujetResponseDTO> findAll(Pageable pageable) {
        return subjectRepository.findAll(pageable)
                .map(SujetResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public SujetResponseDTO findById(Long id) {
        return subjectRepository.findById(id)
                .map(SujetResponseDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", id));
    }

    @Transactional(readOnly = true)
    public Page<SujetResponseDTO> getSubjectsByStatus(SujetStatus status, Pageable pageable) {
        return subjectRepository.findByStatut(status, pageable)
                .map(SujetResponseDTO::fromEntity);
    }

    @Transactional
    public SujetResponseDTO updateSujet(Long id, SujetRequest dto) {
        Sujet sujet = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", id));

        // Ownership Check via Decoupled Security
        String currentUserEmail = securityContext.getCurrentUserEmail();
        boolean isTeacherOwner = sujet.getEnseignant().getEmail().equals(currentUserEmail);
        boolean isStudentOwner = sujet.getProposant() != null
                && sujet.getProposant().getEmail().equals(currentUserEmail);

        if (!isTeacherOwner && !isStudentOwner && !securityContext.hasRole("ADMIN_IT")) {
            throw new UnauthorizedActionException("Vous n'êtes pas autorisé à modifier ce sujet.");
        }

        mapCommonFields(sujet, dto);
        return SujetResponseDTO.fromEntity(subjectRepository.save(sujet));
    }

    @Transactional
    public void deleteSujet(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sujet", id);
        }
        // Potential check: prevent deletion if students are already assigned/validated
        subjectRepository.deleteById(id);
    }

    // --- PROPOSAL LOGIC ---

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

    @Transactional
    public SujetResponseDTO studentProposeSujet(Long teacherId, SujetRequest dto) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        if (!(currentUser instanceof Etudiant student)) {
            throw new UnauthorizedActionException("Seuls les étudiants peuvent proposer des sujets.");
        }

        Enseignant teacher = (Enseignant) userRepository.findById(teacherId)
                .filter(u -> u instanceof Enseignant)
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant", teacherId));

        validateMembersAvailability(student, dto.getPartnerIds());
        Groupe group = groupeService.getOrCreateGroup(student, dto.getPartnerIds());

        Sujet sujet = new Sujet();
        mapCommonFields(sujet, dto);
        sujet.setEnseignant(teacher);
        sujet.setProposant(student);
        sujet.setStatut(SujetStatus.PENDING);

        Sujet savedSujet = subjectRepository.save(sujet);
        createAutomaticCandidature(group, savedSujet);

        return SujetResponseDTO.fromEntity(savedSujet);
    }

    @Transactional(readOnly = true)
    public Page<SujetResponseDTO> getSubjectsByCurrentTeacher(Pageable pageable) {
        return subjectRepository.findByEnseignantId(securityContext.getCurrentUserId(), pageable)
                .map(SujetResponseDTO::fromEntity);
    }

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

        for (Long studentId : allMemberIds) {
            boolean alreadyAffected = candidatureRepository.findByGroupeMembresId(studentId).stream()
                    .anyMatch(c -> c.getStatut() == DemandeStatus.VALIDATED_BY_RESPONSABLE);

            if (alreadyAffected) {
                throw new UnauthorizedActionException("L'étudiant ID " + studentId + " est déjà affecté.");
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