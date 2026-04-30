package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.enums.CreatorRole;
import com.iit.internship_manager.domain.enums.ErrorCode;
import com.iit.internship_manager.domain.enums.MeetingStatus;
import com.iit.internship_manager.domain.exceptions.DomainException;
import com.iit.internship_manager.domain.models.Affectation;
import com.iit.internship_manager.domain.models.RendezVous;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.repositories.AffectationRepository;
import com.iit.internship_manager.repositories.RendezVousRepository;
import com.iit.internship_manager.services.interfaces.IRendezVousService;
import com.iit.internship_manager.services.interfaces.ISecurityContext;
import com.iit.internship_manager.web.dtos.RendezVousRequest;
import com.iit.internship_manager.web.dtos.RendezVousResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RendezVousServiceImpl implements IRendezVousService {

    private final RendezVousRepository rendezVousRepository;
    private final AffectationRepository affectationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ISecurityContext securityContext;

    @Override
    @Transactional
    public RendezVousResponseDTO createMeeting(RendezVousRequest request) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        Affectation aff = affectationRepository.findById(request.getAffectationId())
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Affectation not found"));

        validateAccess(aff, currentUser.getEmail());

        CreatorRole role = aff.getEncadrant().getEmail().equals(currentUser.getEmail())
                ? CreatorRole.ENSEIGNANT
                : CreatorRole.ETUDIANT;

        // Conflict check
        if (rendezVousRepository.existsByTeacherEmailAndDateBetween(
                aff.getEncadrant().getEmail(), request.getDateHeure().minusMinutes(59),
                request.getDateHeure().plusMinutes(59))) {
            throw new DomainException(ErrorCode.CONFLICT, "Teacher is already booked at this time.");
        }

        RendezVous rdv = RendezVous.builder()
                .dateHeure(request.getDateHeure()).lieu(request.getLieu()).objet(request.getObjet())
                .affectation(aff).creePar(role).creatorName(currentUser.getNom() + " " + currentUser.getPrenom())
                .status(role == CreatorRole.ENSEIGNANT ? MeetingStatus.CONFIRMED : MeetingStatus.PENDING)
                .build();

        return saveAndBroadcast(rdv);
    }

    @Override
    @Transactional
    public RendezVousResponseDTO updateStatus(Long meetingId, MeetingStatus newStatus) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        RendezVous rdv = rendezVousRepository.findById(meetingId)
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found"));

        validateAccess(rdv.getAffectation(), currentUser.getEmail());

        // Business Logic: Confirmed meetings can't be refused, only cancelled
        if (rdv.getStatus() == MeetingStatus.CONFIRMED && newStatus == MeetingStatus.REFUSED) {
            throw new DomainException(ErrorCode.CONFLICT, "Confirmed meetings must be cancelled, not refused.");
        }

        rdv.setStatus(newStatus);
        return saveAndBroadcast(rdv);
    }

    @Override
    @Transactional
    public void deleteOrCancel(Long meetingId) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        RendezVous rdv = rendezVousRepository.findById(meetingId)
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found"));

        validateAccess(rdv.getAffectation(), currentUser.getEmail());

        if (rdv.getStatus() == MeetingStatus.CONFIRMED) {
            rdv.setStatus(MeetingStatus.CANCELLED);
            saveAndBroadcast(rdv);
        } else {
            rendezVousRepository.delete(rdv);
            messagingTemplate.convertAndSend("/topic/affectation/" + rdv.getAffectation().getId() + "/meetings/delete",
                    meetingId);
        }
    }

    @Override
    public Page<RendezVousResponseDTO> getAllMeetingsByAffectation(Long affectationId, Pageable pageable) {
        validateAccess(affectationRepository.findById(affectationId).orElseThrow(),
                securityContext.getCurrentUser().getEmail());
        return rendezVousRepository.findByAffectationId(affectationId, pageable).map(this::mapToDTO);
    }

    private void validateAccess(Affectation aff, String email) {
        boolean authorized = aff.getEncadrant().getEmail().equals(email) ||
                aff.getGroupe().getMembres().stream().anyMatch(m -> m.getEmail().equals(email));
        if (!authorized)
            throw new DomainException(ErrorCode.FORBIDDEN, "Unauthorized access.");
    }

    private RendezVousResponseDTO saveAndBroadcast(RendezVous rdv) {
        RendezVous saved = rendezVousRepository.save(rdv);
        RendezVousResponseDTO dto = mapToDTO(saved);
        messagingTemplate.convertAndSend("/topic/affectation/" + rdv.getAffectation().getId() + "/meetings", dto);
        return dto;
    }

    private RendezVousResponseDTO mapToDTO(RendezVous rdv) {
        return RendezVousResponseDTO.builder()
                .id(rdv.getId()).dateHeure(rdv.getDateHeure()).lieu(rdv.getLieu()).objet(rdv.getObjet())
                .creePar(rdv.getCreePar()).creatorName(rdv.getCreatorName())
                .status(rdv.getStatus()).affectationId(rdv.getAffectation().getId()).build();
    }
}