package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.exceptions.DomainException;
import com.iit.internship_manager.domain.models.*;
import com.iit.internship_manager.infrastucture.config.ICalUtils;
import com.iit.internship_manager.repositories.*;
import com.iit.internship_manager.services.interfaces.*;
import com.iit.internship_manager.web.dtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RendezVousServiceImpl implements IRendezVousService {

    private final RendezVousRepository rendezVousRepository;
    private final AffectationRepository affectationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ISecurityContext securityContext;
    private final IEmailService emailService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    @Transactional
    public RendezVousResponseDTO updateStatus(Long meetingId, MeetingStatus newStatus) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        RendezVous rdv = rendezVousRepository.findById(meetingId)
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found"));

        validateAccess(rdv.getAffectation(), currentUser.getEmail());

        // RULE: Cannot change status from CANCELLED or REFUSED back to anything else
        if (rdv.getStatus() == MeetingStatus.CANCELLED || rdv.getStatus() == MeetingStatus.REFUSED) {
            throw new DomainException(ErrorCode.CONFLICT,
                    "Cannot modify a meeting that has already been cancelled or refused.");
        }

        if (newStatus == MeetingStatus.CONFIRMED) {
            validateNotCreator(rdv, currentUser);

            // Logic: Check for conflicts only against active (Pending/Confirmed) meetings
            checkTeacherAvailability(rdv.getAffectation().getEncadrant().getEmail(), rdv.getDateHeure(), rdv.getId());

            byte[] inviteIcal = ICalUtils.generateMeetingInvite(rdv);
            sendNotification(rdv, "meeting-confirmed", "Meeting Confirmed: " + rdv.getObjet(), inviteIcal, currentUser);

        } else if (newStatus == MeetingStatus.CANCELLED) {
            if (rdv.getStatus() != MeetingStatus.CONFIRMED) {
                throw new DomainException(ErrorCode.CONFLICT, "Only confirmed meetings can be cancelled.");
            }
            byte[] cancelIcal = ICalUtils.generateMeetingCancellation(rdv);
            sendNotification(rdv, "meeting-cancelled", "Meeting Cancelled: " + rdv.getObjet(), cancelIcal, currentUser);
        }

        rdv.setStatus(newStatus);
        return saveAndBroadcast(rdv);
    }

    @Override
    @Transactional
    public RendezVousResponseDTO createMeeting(RendezVousRequest request) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        Affectation aff = affectationRepository.findById(request.getAffectationId())
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Affectation not found"));

        validateAccess(aff, currentUser.getEmail());

        // Check availability against existing active meetings before creating a new
        // pending one
        checkTeacherAvailability(aff.getEncadrant().getEmail(), request.getDateHeure(), null);

        CreatorRole role = aff.getEncadrant().getEmail().equals(currentUser.getEmail())
                ? CreatorRole.ENSEIGNANT
                : CreatorRole.ETUDIANT;

        RendezVous rdv = RendezVous.builder()
                .dateHeure(request.getDateHeure())
                .lieu(request.getLieu())
                .objet(request.getObjet())
                .affectation(aff)
                .creePar(role)
                .creatorName(currentUser.getNom() + " " + currentUser.getPrenom())
                .status(MeetingStatus.PENDING)
                .build();

        return saveAndBroadcast(rdv);
    }

    private void checkTeacherAvailability(String teacherEmail, java.time.LocalDateTime dateTime, Long currentId) {
        Long idToExclude = (currentId != null) ? currentId : -1L;

        // Define "Active" statuses that count as a conflict
        Set<MeetingStatus> activeStatuses = new HashSet<>(
                Arrays.asList(MeetingStatus.PENDING, MeetingStatus.CONFIRMED));

        boolean conflict = rendezVousRepository.existsByAffectationEncadrantEmailAndDateHeureBetweenAndIdNotAndStatusIn(
                teacherEmail,
                dateTime.minusMinutes(59),
                dateTime.plusMinutes(59),
                idToExclude,
                activeStatuses);

        if (conflict) {
            throw new DomainException(ErrorCode.CONFLICT,
                    "Teacher is already booked with a pending or confirmed meeting within an hour.");
        }
    }

    private void sendNotification(RendezVous rdv, String template, String subject, byte[] attachment,
            Utilisateur actor) {
        Context context = new Context();
        context.setVariable("actorName", actor.getNom() + " " + actor.getPrenom());
        context.setVariable("objet", rdv.getObjet());
        context.setVariable("date", rdv.getDateHeure().format(formatter));
        context.setVariable("lieu", rdv.getLieu());

        Set<String> recipients = new HashSet<>();
        recipients.add(rdv.getAffectation().getEncadrant().getEmail());
        rdv.getAffectation().getGroupe().getMembres().forEach(m -> recipients.add(m.getEmail()));

        for (String email : recipients) {
            emailService.sendEmail(email, subject, template, context, attachment);
        }
    }

    @Override
    @Transactional
    public void delete(Long meetingId) {
        Utilisateur currentUser = securityContext.getCurrentUser();
        RendezVous rdv = rendezVousRepository.findById(meetingId)
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found"));

        validateAccess(rdv.getAffectation(), currentUser.getEmail());

        if (rdv.getStatus() == MeetingStatus.CONFIRMED) {
            throw new DomainException(ErrorCode.CONFLICT, "Please cancel the meeting before deleting it.");
        }

        rendezVousRepository.delete(rdv);
        messagingTemplate.convertAndSend("/topic/affectation/" + rdv.getAffectation().getId() + "/meetings/delete",
                meetingId);
    }

    private void validateNotCreator(RendezVous rdv, Utilisateur currentUser) {
        boolean isTeacherCreator = rdv.getCreePar() == CreatorRole.ENSEIGNANT &&
                rdv.getAffectation().getEncadrant().getEmail().equals(currentUser.getEmail());

        boolean isStudentCreator = rdv.getCreePar() == CreatorRole.ETUDIANT &&
                rdv.getAffectation().getGroupe().getMembres().stream()
                        .anyMatch(m -> m.getEmail().equals(currentUser.getEmail()));

        if (isTeacherCreator || isStudentCreator) {
            throw new DomainException(ErrorCode.FORBIDDEN, "You cannot accept a meeting you created.");
        }
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
                .id(rdv.getId())
                .dateHeure(rdv.getDateHeure())
                .lieu(rdv.getLieu())
                .objet(rdv.getObjet())
                .creePar(rdv.getCreePar())
                .creatorName(rdv.getCreatorName())
                .status(rdv.getStatus())
                .affectationId(rdv.getAffectation().getId())
                .build();
    }

    @Override
    public Page<RendezVousResponseDTO> getAllMeetingsByAffectation(Long affectationId, Pageable pageable) {
        Affectation aff = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Affectation not found"));

        validateAccess(aff, securityContext.getCurrentUser().getEmail());
        return rendezVousRepository.findByAffectationId(affectationId, pageable).map(this::mapToDTO);
    }
}