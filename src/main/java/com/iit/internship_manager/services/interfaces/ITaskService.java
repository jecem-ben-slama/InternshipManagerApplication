package com.iit.internship_manager.services.interfaces;

import com.iit.internship_manager.web.dtos.TaskRequest;
import com.iit.internship_manager.web.dtos.TaskResponseDTO;
import com.iit.internship_manager.domain.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITaskService {
    TaskResponseDTO createTask(TaskRequest request);

    Page<TaskResponseDTO> getTasksByAffectation(Long affectationId, Pageable pageable);

    TaskResponseDTO updateTaskStatus(Long taskId, TaskStatus status);

    void deleteTask(Long taskId);

    // Useful for your progress bar in Flutter
    double getInternshipProgress(Long affectationId);
}
