package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.*;
import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.exceptions.*;
import com.iit.internship_manager.repositories.*;
import com.iit.internship_manager.web.dtos.SujetRequest;
import com.iit.internship_manager.web.dtos.SujetResponseDTO;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final CandidatureRepository candidatureRepository;
    private final GroupeRepository groupeRepository; 
    
    // New repository for Groupes
    private Groupe createGroupForProposal(Etudiant creator, List<Long> partnerIds) {
        // 1. Build the list of all intended members
        List<Etudiant> targetMembers = new ArrayList<>();
        targetMembers.add(creator);

        if (partnerIds != null && !partnerIds.isEmpty()) {
            List<Etudiant> partners = userRepository.findAllById(partnerIds)
                    .stream()
                    .map(u -> (Etudiant) u)
                    .toList();
            targetMembers.addAll(partners);
        }

        // 2. CHECK IF THIS EXACT GROUP ALREADY EXISTS
        // We look for a group where the creator is a member and check if the total
        // members match
        List<Groupe> existingGroupsForCreator = groupeRepository.findByMembresId(creator.getId());

        for (Groupe existingGroup : existingGroupsForCreator) {
            List<Long> existingMemberIds = existingGroup.getMembres().stream()
                    .map(Etudiant::getId)
                    .toList();

            List<Long> targetMemberIds = targetMembers.stream()
                    .map(Etudiant::getId)
                    .toList();

            // Check if both lists contain the same IDs and are the same size
            if (existingMemberIds.size() == targetMemberIds.size() &&
                    existingMemberIds.containsAll(targetMemberIds)) {
                return existingGroup; // Found it! Return the existing group instead of saving a new one
            }
        }

        // 3. If no matching group is found, create and save a new one
        String groupName = "Binôme: " + creator.getNom();
        if (targetMembers.size() > 1) {
            groupName += " & " + targetMembers.get(1).getNom();
        }

        Groupe group = Groupe.builder()
                .nom(groupName)
                .membres(targetMembers)
                .build();

        return groupeRepository.save(group);
    }
  /**
     * Case A: Teacher proposes their own subject.
     * Enseignant_id = Teacher, Proposant_id = NULL, Statut = AVAILABLE.
     */
    @Transactional
    public SujetResponseDTO teacherProposeSujet(SujetRequest dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Enseignant teacher = (Enseignant) userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedActionException("Enseignant non trouvé"));

        Sujet sujet = new Sujet();
        mapCommonFields(sujet, dto);

        sujet.setEnseignant(teacher);
        sujet.setProposant(null);
        sujet.setStatut(SujetStatus.AVAILABLE); // Scenario 2: Validated by default

        return SujetResponseDTO.fromEntity(subjectRepository.save(sujet));
    }

    /**
     * Case B: Student proposes to a specific teacher.
     * Enseignant_id = Teacher, Proposant_id = Student, Statut = PENDING.
     */
  @Transactional
public SujetResponseDTO studentProposeSujet(Long teacherId, SujetRequest dto) {
    // 1. Identify the student who is currently logged in
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    Utilisateur currentUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedActionException("Utilisateur non trouvé"));

    if (!(currentUser instanceof Etudiant)) {
        throw new UnauthorizedActionException("Seuls les étudiants peuvent proposer des sujets.");
    }
    Etudiant creator = (Etudiant) currentUser;

    // 2. Identify the target teacher (Safe Check)
    Utilisateur targetUser = userRepository.findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Enseignant", teacherId));
    
    if (!(targetUser instanceof Enseignant)) {
        throw new BadRequestException("L'ID fourni n'appartient pas à un enseignant.");
    }
    Enseignant teacher = (Enseignant) targetUser;

    // 3. CHECK IF GROUP MEMBERS ARE ALREADY AFFECTED
    // We check the creator + all partners in the DTO
    List<Long> allMemberIds = new ArrayList<>();
    allMemberIds.add(creator.getId());
    if (dto.getPartnerIds() != null) {
        allMemberIds.addAll(dto.getPartnerIds());
    }

    for (Long studentId : allMemberIds) {
        // We check if a VALIDATED candidature exists for any member
        boolean alreadyAffected = candidatureRepository.findByGroupeMembresId(studentId).stream()
                .anyMatch(c -> c.getStatut() == DemandeStatus.VALIDATED_BY_RESPONSABLE);
        
        if (alreadyAffected) {
            throw new UnauthorizedActionException("L'un des membres (ID: " + studentId + ") est déjà affecté à un sujet.");
        }
    }

    // 4. Create or find the Group (Binôme)
    Groupe group = createGroupForProposal(creator, dto.getPartnerIds());

    // 5. Map and save the Subject
    Sujet sujet = new Sujet();
    mapCommonFields(sujet, dto);
    sujet.setEnseignant(teacher);
    sujet.setProposant(creator); 
    sujet.setStatut(SujetStatus.PENDING);

    Sujet savedSujet = subjectRepository.save(sujet);

    // 6. Create the Candidature linked to the GROUP
    Candidature autoCap = new Candidature();
    autoCap.setGroupe(group);
    autoCap.setSujet(savedSujet);
    autoCap.setStatut(DemandeStatus.PENDING);
    candidatureRepository.save(autoCap);

    return SujetResponseDTO.fromEntity(savedSujet);
}
    
    /**
     * Fix for: The method getSubjectsByStatus(SujetStatus, Pageable) is undefined
     */
    @Transactional(readOnly = true)
    public Page<SujetResponseDTO> getSubjectsByStatus(SujetStatus status, Pageable pageable) {
        return subjectRepository.findByStatut(status, pageable)
                .map(SujetResponseDTO::fromEntity);
    }

    /**
     * Fix for: The method findById(Long) is undefined
     */
    @Transactional(readOnly = true)
    public SujetResponseDTO findById(Long id) {
        return subjectRepository.findById(id)
                .map(SujetResponseDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", id));
    }

    @Transactional(readOnly = true)
    public Page<SujetResponseDTO> getSubjectsByCurrentTeacher(Pageable pageable) {
        // 1. Get the email from the JWT token via SecurityContext
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Find the teacher in the database
        Enseignant teacher = (Enseignant) userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedActionException("Enseignant non trouvé"));

        // 3. Use the ID we just found to query the repository
        return subjectRepository.findByEnseignantId(teacher.getId(), pageable)
                .map(SujetResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<SujetResponseDTO> findAll(Pageable pageable) {
        return subjectRepository.findAll(pageable)
                .map(SujetResponseDTO::fromEntity);
    }

    /**
     * Admin/Responsable action to override or validate status if needed.
     */
    @Transactional
    public SujetResponseDTO updateSujetStatus(Long id, SujetStatus status) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur currentUser = userRepository.findByEmail(email).orElseThrow();

        // Check if user is the Coordinator (ResponsablePFE)
        if (!(currentUser instanceof Enseignant) || !((Enseignant) currentUser).isResponsablePFE()) {
            throw new UnauthorizedActionException("Seul le Responsable PFE peut effectuer cette action.");
        }

        Sujet sujet = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", id));

        sujet.setStatut(status);
        return SujetResponseDTO.fromEntity(subjectRepository.save(sujet));
    }

    @Transactional
    public SujetResponseDTO updateSujet(Long id, SujetRequest dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Sujet sujet = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sujet", id));

        // Check ownership: Can be the teacher (enseignant) or the student (proposant)
        boolean isTeacherOwner = sujet.getEnseignant().getEmail().equals(email);
        boolean isStudentOwner = sujet.getProposant() != null && sujet.getProposant().getEmail().equals(email);

        if (!isTeacherOwner && !isStudentOwner) {
            throw new UnauthorizedActionException("Vous n'avez pas le droit de modifier ce sujet.");
        }

        mapCommonFields(sujet, dto);
        return SujetResponseDTO.fromEntity(subjectRepository.save(sujet));
    }

    @Transactional
    public void deleteSujet(Long sujetId) {
        if (!subjectRepository.existsById(sujetId)) {
            throw new ResourceNotFoundException("Sujet", sujetId);
        }
        subjectRepository.deleteById(sujetId);
    }

    private void mapCommonFields(Sujet sujet, SujetRequest dto) {
        sujet.setTitre(dto.getTitre());
        sujet.setDescription(dto.getDescription());
        sujet.setTechnologies(dto.getTechnologies());
        sujet.setType(dto.getType()); // PFA or PFE
    }
}