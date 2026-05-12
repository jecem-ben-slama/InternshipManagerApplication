package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.enums.CreatorRole;
import com.iit.internship_manager.domain.enums.ErrorCode;
import com.iit.internship_manager.domain.enums.TaskStatus;
import com.iit.internship_manager.domain.exceptions.DomainException;
import com.iit.internship_manager.domain.models.*;
import com.iit.internship_manager.repositories.AffectationRepository;
import com.iit.internship_manager.repositories.TaskRepository;
import com.iit.internship_manager.services.interfaces.ITaskService;
import com.iit.internship_manager.services.interfaces.ISecurityContext;
import com.iit.internship_manager.web.dtos.TaskRequest;
import com.iit.internship_manager.web.dtos.TaskResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements ITaskService {

    private final TaskRepository taskRepository;
    private final AffectationRepository affectationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ISecurityContext securityContext;

    @Override
    @Transactional
    public TaskResponseDTO createTask(TaskRequest request) {
        Utilisateur currentUser = securityContext.getCurrentUser();

        // Check if Affectation exists
        Affectation affectation = affectationRepository.findById(request.getAffectationId())
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Affectation not found"));

        // Centralized security check
        validateAccess(affectation, currentUser.getEmail());
        validateTeacherCreator(affectation, currentUser);

        // Determine role for task creation
        CreatorRole role = affectation.getEncadrant().getEmail().equals(currentUser.getEmail())
                ? CreatorRole.ENSEIGNANT
                : CreatorRole.ETUDIANT;

        Task task = Task.builder()
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .priorite(request.getPriorite())
                .creePar(role)
                .creatorName(currentUser.getNom() + " " + currentUser.getPrenom())
                .affectation(affectation)
                .status(TaskStatus.PENDING)
                .build();

        Task savedTask = taskRepository.save(task);
        TaskResponseDTO response = mapToResponseDTO(savedTask);

        messagingTemplate.convertAndSend("/topic/affectation/" + affectation.getId() + "/tasks", response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> getTasksByAffectation(Long affectationId, Pageable pageable) {
        Utilisateur currentUser = securityContext.getCurrentUser();

        // Check if Affectation exists
        Affectation affectation = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Affectation not found"));

        // Centralized security check
        validateAccess(affectation, currentUser.getEmail());

        boolean isTeacher = affectation.getEncadrant().getEmail().equals(currentUser.getEmail());
        Page<Task> page = isTeacher
                ? taskRepository.findByAffectationId(affectationId, pageable)
                : taskRepository.findByAffectationIdAndCreePar(affectationId, CreatorRole.ENSEIGNANT, pageable);

        return page
                .map(this::mapToResponseDTO);
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTaskStatus(Long taskId, TaskStatus status) {
        Utilisateur currentUser = securityContext.getCurrentUser();

        // Check if Task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Task not found"));

        // Centralized security check (checks if user is in the affectation tied to this
        // task)
        validateAccess(task.getAffectation(), currentUser.getEmail());

        // Specific Business Rule: ONLY students can update status
        boolean isStudent = task.getAffectation().getGroupe().getMembres().stream()
                .anyMatch(m -> m.getEmail().equals(currentUser.getEmail()));

        if (!isStudent) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Only assigned students can mark tasks as completed.");
        }

        task.setStatus(status);
        Task savedTask = taskRepository.save(task);
        TaskResponseDTO response = mapToResponseDTO(savedTask);

        messagingTemplate.convertAndSend("/topic/affectation/" + task.getAffectation().getId() + "/tasks", response);
        return response;
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        Utilisateur currentUser = securityContext.getCurrentUser();

        // Check if Task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Task not found"));

        // Centralized security check
        validateAccess(task.getAffectation(), currentUser.getEmail());
        validateTeacherCreator(task.getAffectation(), currentUser);

        taskRepository.delete(task);
    }

    @Override
    @Transactional(readOnly = true)
    public double getInternshipProgress(Long affectationId) {
        Utilisateur currentUser = securityContext.getCurrentUser();

        // Check if Affectation exists
        Affectation affectation = affectationRepository.findById(affectationId)
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND, "Affectation not found"));

        // Centralized security check
        validateAccess(affectation, currentUser.getEmail());

        boolean isTeacher = affectation.getEncadrant().getEmail().equals(currentUser.getEmail());
        long total = isTeacher
                ? taskRepository.countByAffectationId(affectationId)
                : taskRepository.countByAffectationIdAndCreePar(affectationId, CreatorRole.ENSEIGNANT);
        if (total == 0)
            return 0.0;

        long completed = isTeacher
                ? taskRepository.countByAffectationIdAndCompletedTrue(affectationId)
                : taskRepository.countByAffectationIdAndCreeParAndCompletedTrue(affectationId, CreatorRole.ENSEIGNANT);
        return (double) completed / total * 100;
    }

    /**
     * Centralized security check
     */
    private void validateAccess(Affectation aff, String email) {
        boolean isTeacher = aff.getEncadrant().getEmail().equals(email);
        boolean isStudent = aff.getGroupe().getMembres().stream()
                .anyMatch(m -> m.getEmail().equals(email));

        if (!isTeacher && !isStudent) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Access denied. You are not assigned to this internship.");
        }
    }

    private void validateTeacherCreator(Affectation affectation, Utilisateur currentUser) {
        if (!affectation.getEncadrant().getEmail().equals(currentUser.getEmail())) {
            throw new DomainException(ErrorCode.FORBIDDEN, "Only the assigned teacher can create or delete tasks.");
        }
    }

    private TaskResponseDTO mapToResponseDTO(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .description(task.getDescription())
                .completed(task.isCompleted())
                .status(task.getStatus())
                .deadline(task.getDeadline())
                .priorite(task.getPriorite())
                .creePar(task.getCreePar())
                .creatorName(task.getCreatorName())
                .createdAt(task.getCreatedAt())
                .affectationId(task.getAffectation().getId())
                .build();
    }
}
