package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.exceptions.UnauthorizedActionException;
import com.iit.internship_manager.domain.models.Affectation;
import com.iit.internship_manager.domain.models.RendezVous;
import com.iit.internship_manager.repositories.AffectationRepository;
import com.iit.internship_manager.repositories.RendezVousRepository;
import com.iit.internship_manager.services.interfaces.IRendezVousService;
import com.iit.internship_manager.web.dtos.RendezVousRequest;
import com.iit.internship_manager.web.dtos.RendezVousResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RendezVousServiceImpl implements IRendezVousService {

    private final RendezVousRepository rendezVousRepository;
    private final AffectationRepository affectationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public RendezVousResponseDTO createMeeting(RendezVousRequest request, String currentUserEmail) {
        // 1. Basic Date Validation
        if (request.getDateHeure().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot schedule a meeting in the past.");
        }

        Affectation affectation = affectationRepository.findById(request.getAffectationId())
                .orElseThrow(() -> new EntityNotFoundException("Affectation not found"));

        // 2. SECURITY: Check if user is part of the affectation (Teacher or Student)
        boolean isTeacher = affectation.getEncadrant().getEmail().equals(currentUserEmail);
        boolean isStudent = affectation.getGroupe().getMembres().stream()
                .anyMatch(m -> m.getEmail().equals(currentUserEmail));

        if (!isTeacher && !isStudent) {
            throw new UnauthorizedActionException("You are not part of this internship.");
        }

        // 3. CONFLICT CHECK: Check if teacher has a meeting ±1 hour
        // Start check: 59 mins before | End check: 59 mins after
        LocalDateTime bufferStart = request.getDateHeure().minusMinutes(59);
        LocalDateTime bufferEnd = request.getDateHeure().plusMinutes(59);

        boolean hasConflict = rendezVousRepository.existsByTeacherEmailAndDateBetween(
                affectation.getEncadrant().getEmail(),
                bufferStart,
                bufferEnd);

        if (hasConflict) {
            throw new IllegalStateException("The teacher already has a meeting scheduled within this hour.");
        }

        // 4. Build and Save
        RendezVous rdv = RendezVous.builder()
                .dateHeure(request.getDateHeure())
                .lieu(request.getLieu())
                .affectation(affectation)
                .estConfirme(isTeacher) // Auto-confirm if teacher creates it
                .build();

        RendezVous saved = rendezVousRepository.save(rdv);
        RendezVousResponseDTO response = mapToDTO(saved);

        // 5. WebSocket Broadcast
        messagingTemplate.convertAndSend("/topic/affectation/" + affectation.getId() + "/meetings", response);

        return response;
    }

    @Override
    @Transactional
    public RendezVousResponseDTO confirmMeeting(Long meetingId, String currentUserEmail) {
        RendezVous rdv = rendezVousRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));

        // Security: Only a student in the group can confirm
        boolean isStudent = rdv.getAffectation().getGroupe().getMembres().stream()
                .anyMatch(m -> m.getEmail().equals(currentUserEmail));

        if (!isStudent) {
            throw new UnauthorizedActionException("Only assigned students can confirm this meeting.");
        }

        rdv.setEstConfirme(true);
        RendezVous saved = rendezVousRepository.save(rdv);
        RendezVousResponseDTO response = mapToDTO(saved);

        // Notify teacher that student confirmed
        messagingTemplate.convertAndSend("/topic/affectation/" + rdv.getAffectation().getId() + "/meetings", response);

        return response;
    }

    @Override
    public Page<RendezVousResponseDTO> getAllMeetingsByAffectation(Long affectationId, Pageable pageable) {
        return rendezVousRepository.findByAffectationId(affectationId, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public void cancelMeeting(Long meetingId, String currentUserEmail) {
        RendezVous rdv = rendezVousRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found"));

        // Ensure user is part of the affectation before deleting
        if (!rdv.getAffectation().getEncadrant().getEmail().equals(currentUserEmail)) {
            throw new UnauthorizedActionException("Unauthorized to cancel this meeting.");
        }

        rendezVousRepository.delete(rdv);
    }

    private RendezVousResponseDTO mapToDTO(RendezVous rdv) {
        return RendezVousResponseDTO.builder()
                .id(rdv.getId())
                .dateHeure(rdv.getDateHeure())
                .lieu(rdv.getLieu())
                .estConfirme(rdv.isEstConfirme())
                .affectationId(rdv.getAffectation().getId())
                .build();
    }
}