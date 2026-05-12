package com.iit.internship_manager.services.interfaces;

import com.iit.internship_manager.domain.enums.DemandeStatus;
import com.iit.internship_manager.web.dtos.CandidatureResponseDTO;
import org.springframework.data.domain.Page;
import java.util.List;

public interface ICandidatureService {
    void accepterEtudiant(Long candidatureId);

    CandidatureResponseDTO findById(Long candidatureId);

    Page<CandidatureResponseDTO> getCandidaturesByYear(String yearId, DemandeStatus status, int page, int size);
    void refuserEtudiant(Long candidatureId);

    void demanderClarification(Long candidatureId, String justification);

    void postuler(Long sujetId, List<Long> partnerIds);

    void annulerCandidature(Long id);

    Page<CandidatureResponseDTO> getPagedCandidatures(DemandeStatus status, int page, int size);
}
