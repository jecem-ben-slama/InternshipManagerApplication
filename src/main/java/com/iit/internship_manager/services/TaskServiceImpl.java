package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.exceptions.UnauthorizedActionException;
import com.iit.internship_manager.domain.models.Affectation;
import com.iit.internship_manager.domain.models.Task;
import com.iit.internship_manager.repositories.AffectationRepository;
import com.iit.internship_manager.repositories.TaskRepository;
import com.iit.internship_manager.services.interfaces.ITaskService;
import com.iit.internship_manager.web.dtos.TaskRequest;
import com.iit.internship_manager.web.dtos.TaskResponseDTO;
import jakarta.persistence.EntityNotFoundException;
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
    private final SimpMessagingTemplate messagingTemplate; // <--- Add this

    @Override
    @Transactional
    public TaskResponseDTO createTask(TaskRequest request, String currentUserEmail) {
        // 1. Fetch the parent Affectation
        Affectation affectation = affectationRepository.findById(request.getAffectationId())
                .orElseThrow(() -> new EntityNotFoundException("Affectation not found"));

        // 2. SECURITY CHECK: Is the current user part of this affectation?
        boolean isTeacher = affectation.getEncadrant().getEmail().equals(currentUserEmail);
        boolean isStudentInGroup = affectation.getGroupe().getMembres().stream()
                .anyMatch(membre -> membre.getEmail().equals(currentUserEmail));

        if (!isTeacher && !isStudentInGroup) {
            throw new UnauthorizedActionException("You are not authorized to add tasks to this internship.");
        }

        // 3. Build the Task entity
        Task task = Task.builder()
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .priorite(request.getPriorite())
                .creePar(request.getCreePar())
                .affectation(affectation)
                .completed(false)
                .build();

        // 4. Save and map
        Task savedTask = taskRepository.save(task);
        TaskResponseDTO response = mapToResponseDTO(savedTask);

        // 5. REAL-TIME BROADCAST
        String destination = "/topic/affectation/" + affectation.getId() + "/tasks";
        messagingTemplate.convertAndSend(destination, response);

        return response;
    }
    @Override
    public Page<TaskResponseDTO> getTasksByAffectation(Long affectationId, Pageable pageable) {
        return taskRepository.findByAffectationId(affectationId, pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTaskStatus(Long taskId, boolean completed) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        task.setCompleted(completed);
        return mapToResponseDTO(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    @Override
    public double getInternshipProgress(Long affectationId) {
        long total = taskRepository.countByAffectationId(affectationId);
        if (total == 0)
            return 0.0;
        long completed = taskRepository.countByAffectationIdAndCompletedTrue(affectationId);
        return (double) completed / total * 100;
    }

    // Manual Mapper (Clean Code - no need for extra libraries yet)
    private TaskResponseDTO mapToResponseDTO(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .description(task.getDescription())
                .completed(task.isCompleted())
                .deadline(task.getDeadline())
                .priorite(task.getPriorite())
                .creePar(task.getCreePar())
                .createdAt(task.getCreatedAt())
                .affectationId(task.getAffectation().getId())
                .build();
    }
}