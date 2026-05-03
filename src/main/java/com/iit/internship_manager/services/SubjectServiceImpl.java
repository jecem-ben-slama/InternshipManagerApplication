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
    private final AcademicYearRepository academicYearRepository;
    private final AffectationRepository affectationRepository;
    private final IGroupeService groupeService;
    private final ISecurityContext securityContext;
    private final CurrentYearProvider currentYearProvider;

    @Override
    @Transactional(readOnly = true)
    public Page<SujetResponseDTO> findAll(Pageable pageable) {
        AcademicYear currentYear = currentYearProvider.getCurrent();
        DepartmentType userDept = resolveUserDepartment();

        return subjectRepository.findByDepartmentAndAnneeUniversitaire(userDept, currentYear, pageable)
                .map(SujetResponseDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public SujetResponseDTO findById(Long id) {
        Sujet sujet = subjectRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", id));

      

        return SujetResponseDTO.fromEntity(sujet);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SujetResponseDTO> getSubjectsByYear(String yearId, Pageable pageable) {
        AcademicYear year = academicYearRepository.findById(yearId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Année universitaire " + yearId + " introuvable", null));

        DepartmentType userDept = resolveUserDepartment();
        return subjectRepository.findByDepartmentAndAnneeUniversitaire(userDept, year, pageable)
                .map(SujetResponseDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SujetResponseDTO> getSubjectsByStatus(SujetStatus status, Pageable pageable) {
        DepartmentType userDept = resolveUserDepartment();
        AcademicYear currentYear = currentYearProvider.getCurrent();

        return subjectRepository.findByStatutAndDepartmentAndAnneeUniversitaire(
                status, userDept, currentYear, pageable)
                .map(SujetResponseDTO::fromEntity);
    }

    @Override
    @Transactional
    public SujetResponseDTO updateSujet(Long id, SujetRequest dto) {
        Sujet sujet = subjectRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", id));

        validateOwnershipAndDept(sujet);

        if (sujet.getStatut() == SujetStatus.TAKEN) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Ce sujet est déjà assigné.");
        }

        mapCommonFields(sujet, dto);
        return SujetResponseDTO.fromEntity(subjectRepository.save(sujet));
    }

    @Override
    @Transactional
    public SujetResponseDTO updateSujetStatus(Long id, SujetStatus status) {
        Utilisateur user = securityContext.getCurrentUser();

        if (!(user instanceof Enseignant e) || !e.isResponsablePFE()) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Action réservée au Responsable PFE.");
        }

        Sujet sujet = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", id));

     

        sujet.setStatut(status);
        return SujetResponseDTO.fromEntity(subjectRepository.save(sujet));
    }

    @Override
    @Transactional
    public void deleteSujet(Long id) {
        Sujet sujet = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", id));

        validateOwnershipAndDept(sujet);

        if (sujet.getStatut() == SujetStatus.TAKEN) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Impossible de supprimer un sujet déjà assigné.");
        }

        if (candidatureRepository.existsBySujetId(id)) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Ce sujet a des candidatures actives.");
        }

        subjectRepository.delete(sujet);
    }

    @Override
    @Transactional
    public SujetResponseDTO teacherProposeSujet(SujetRequest dto) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        if (!(currentUser instanceof Enseignant teacher)) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Seuls les enseignants peuvent proposer des sujets.");
        }

        Sujet sujet = new Sujet();
        mapCommonFields(sujet, dto);
        sujet.setEnseignant(teacher);
        sujet.setStatut(SujetStatus.AVAILABLE);
        sujet.setAnneeUniversitaire(currentYearProvider.getCurrent());

        return SujetResponseDTO.fromEntity(subjectRepository.save(sujet));
    }

    @Override
    @Transactional
    public SujetResponseDTO studentProposeSujet(Long teacherId, SujetRequest dto) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        if (!(currentUser instanceof Etudiant student)) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Seuls les étudiants peuvent proposer des sujets.");
        }

        validateMembersAvailability(student, dto.getPartnerIds());

        Enseignant teacher = userRepository.findEnseignantById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant", teacherId));

        if (!teacher.getDepartment().equals(student.getDepartment())) {
            throw new DomainException(ErrorCode.FORBIDDEN, "L'enseignant choisi n'appartient pas à votre département.");
        }

        Groupe group = groupeService.getOrCreateGroup(student, dto.getPartnerIds());

        Sujet sujet = new Sujet();
        mapCommonFields(sujet, dto);
        sujet.setEnseignant(teacher);
        sujet.setProposant(student);
        sujet.setStatut(SujetStatus.PROPOSED_BY_STUDENT);
        sujet.setAnneeUniversitaire(currentYearProvider.getCurrent());

        Sujet savedSujet = subjectRepository.save(sujet);
        createAutomaticCandidature(group, savedSujet);

        return SujetResponseDTO.fromEntity(savedSujet);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SujetResponseDTO> getSubjectsByCurrentTeacher(Pageable pageable) {
        return subjectRepository.findByEnseignantId(securityContext.getCurrentUserId(), pageable)
                .map(SujetResponseDTO::fromEntity);
    }

    // --- Private Helpers ---

    private DepartmentType resolveUserDepartment() {
        Utilisateur user = securityContext.getCurrentUser();
        if (user instanceof Etudiant e)
            return e.getDepartment();
        if (user instanceof Enseignant t)
            return t.getDepartment();

        throw new DomainException(ErrorCode.FORBIDDEN,
                "Accès refusé : Seuls les étudiants et enseignants sont autorisés.");
    }

    private void validateOwnershipAndDept(Sujet sujet) {
        Utilisateur user = securityContext.getCurrentUser();
        String email = user.getEmail();

        boolean isOwner = (sujet.getEnseignant() != null && sujet.getEnseignant().getEmail().equals(email)) ||
                (sujet.getProposant() != null && sujet.getProposant().getEmail().equals(email));

        if (!isOwner) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Vous n'êtes pas le propriétaire de ce sujet.");
        }

      
    }

    private void validateMembersAvailability(Etudiant creator, List<Long> partnerIds) {
        List<Long> allMemberIds = new ArrayList<>();
        allMemberIds.add(creator.getId());
        if (partnerIds != null)
            allMemberIds.addAll(partnerIds);

        AcademicYear currentYear = currentYearProvider.getCurrent();
        if (affectationRepository.existsByGroupeMembresIdInAndAnneeUniversitaire(allMemberIds, currentYear)) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Un ou plusieurs membres sont déjà affectés.");
        }
    }

    private void createAutomaticCandidature(Groupe group, Sujet sujet) {
        Candidature candidature = new Candidature();
        candidature.setGroupe(group);
        candidature.setSujet(sujet);
        candidature.setStatut(DemandeStatus.PENDING);
        candidature.setAnneeUniversitaire(currentYearProvider.getCurrent());
        candidatureRepository.save(candidature);
    }

    private void mapCommonFields(Sujet sujet, SujetRequest dto) {
        sujet.setTitre(dto.getTitre());
        sujet.setDescription(dto.getDescription());
        sujet.setTechnologies(dto.getTechnologies());
        sujet.setType(dto.getType());
    }
}