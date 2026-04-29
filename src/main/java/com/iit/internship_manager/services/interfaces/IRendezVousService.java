package com.iit.internship_manager.services.interfaces;

import com.iit.internship_manager.web.dtos.RendezVousRequest;
import com.iit.internship_manager.web.dtos.RendezVousResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRendezVousService {
    RendezVousResponseDTO createMeeting(RendezVousRequest request, String currentUserEmail);

    Page<RendezVousResponseDTO> getAllMeetingsByAffectation(Long affectationId, Pageable pageable);

    RendezVousResponseDTO confirmMeeting(Long meetingId, String currentUserEmail);

    void cancelMeeting(Long meetingId, String currentUserEmail);
}