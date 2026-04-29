package com.iit.internship_manager.services.interfaces;

import com.iit.internship_manager.web.dtos.TaskRequest;
import com.iit.internship_manager.web.dtos.TaskResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITaskService {
    TaskResponseDTO createTask(TaskRequest request, String currentUserEmail);

    Page<TaskResponseDTO> getTasksByAffectation(Long affectationId, Pageable pageable);

    TaskResponseDTO updateTaskStatus(Long taskId, boolean completed);

    void deleteTask(Long taskId);

    // Useful for your progress bar in Flutter
    double getInternshipProgress(Long affectationId);
}