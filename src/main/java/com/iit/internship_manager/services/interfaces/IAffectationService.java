package com.iit.internship_manager.services.interfaces;

import com.iit.internship_manager.domain.models.Etudiant;
import com.iit.internship_manager.web.dtos.AffectationResponseDTO;
import com.iit.internship_manager.web.dtos.StudentWorkloadDTO;
import com.iit.internship_manager.web.dtos.TeacherWorkloadDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAffectationService {

    // Returns a page of affectations based on the logged-in user's role
    Page<AffectationResponseDTO> getMyAffectations(Pageable pageable);

    Page<AffectationResponseDTO> getAffectationsByYear(String yearId, Pageable pageable);
    // Returns a page of students who haven't been assigned to a project yet
    Page<Etudiant> getUnassignedStudents(Pageable pageable);

    // Returns a page of all teachers and their current encadrement percentage
    Page<TeacherWorkloadDTO> getTeachersWorkload(Pageable pageable);

    // Returns the detailed view of the project (Subject, Teacher, Coworkers)
    Page<StudentWorkloadDTO> getWorkloadView(Pageable pageable);

    // Operational methods
    void abortAffectation(Long affectationId);

    void completeProject(Long id);

    long getTotalAssignmentsCount();
}