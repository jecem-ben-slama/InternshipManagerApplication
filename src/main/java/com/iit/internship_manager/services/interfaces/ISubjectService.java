package com.iit.internship_manager.services.interfaces;

import com.iit.internship_manager.domain.enums.SujetStatus;
import com.iit.internship_manager.web.dtos.SujetRequest;
import com.iit.internship_manager.web.dtos.SujetResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISubjectService {
    Page<SujetResponseDTO> findAll(Pageable pageable);

    SujetResponseDTO findById(Long id);

    Page<SujetResponseDTO> getSubjectsByStatus(SujetStatus status, Pageable pageable);

    Page<SujetResponseDTO> getSubjectsByCurrentTeacher(Pageable pageable);

    SujetResponseDTO teacherProposeSujet(SujetRequest dto);

    SujetResponseDTO studentProposeSujet(Long teacherId, SujetRequest dto);

    SujetResponseDTO updateSujet(Long id, SujetRequest dto);

    SujetResponseDTO updateSujetStatus(Long id, SujetStatus status);
    Page<SujetResponseDTO> getSubjectsByYear(String yearId, Pageable pageable);

    void deleteSujet(Long id);
}