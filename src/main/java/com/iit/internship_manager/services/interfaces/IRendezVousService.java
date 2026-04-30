package com.iit.internship_manager.services.interfaces;

import com.iit.internship_manager.domain.enums.MeetingStatus;
import com.iit.internship_manager.web.dtos.RendezVousRequest;
import com.iit.internship_manager.web.dtos.RendezVousResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRendezVousService {
    RendezVousResponseDTO createMeeting(RendezVousRequest request);

    Page<RendezVousResponseDTO> getAllMeetingsByAffectation(Long affectationId, Pageable pageable);

    RendezVousResponseDTO updateStatus(Long meetingId, MeetingStatus newStatus);

    void deleteOrCancel(Long meetingId);
}