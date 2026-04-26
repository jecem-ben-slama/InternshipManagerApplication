package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.*;
import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.exceptions.*;
import com.iit.internship_manager.repositories.*;
import com.iit.internship_manager.web.dtos.SujetRequest;
import com.iit.internship_manager.web.dtos.SujetResponseDTO;

import lombok.RequiredArgsConstructor;
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
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Etudiant student = (Etudiant) userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedActionException("Étudiant non trouvé"));

        Enseignant teacher = (Enseignant) userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant", teacherId));

        Sujet sujet = new Sujet();
        mapCommonFields(sujet, dto);

        sujet.setEnseignant(teacher);
        sujet.setProposant(student);
        sujet.setStatut(SujetStatus.PENDING); // Needs teacher review

        Sujet savedSujet = subjectRepository.save(sujet);

        // Auto-create candidature so teacher sees it immediately
        Candidature autoCap = new Candidature();
        autoCap.setEtudiant(student);
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