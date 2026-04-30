package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.services.interfaces.ITaskService;
import com.iit.internship_manager.web.dtos.TaskRequest;
import com.iit.internship_manager.web.dtos.TaskResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final ITaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody TaskRequest request) {
        return new ResponseEntity<>(taskService.createTask(request), HttpStatus.CREATED);
    }

    @GetMapping("/affectation/{affectationId}")
    public ResponseEntity<Page<TaskResponseDTO>> getTasks(
            @PathVariable Long affectationId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(taskService.getTasksByAffectation(affectationId, pageable));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskResponseDTO> updateStatus(
            @PathVariable Long taskId,
            @RequestParam boolean completed) {
        return ResponseEntity.ok(taskService.updateTaskStatus(taskId, completed));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/affectation/{affectationId}/progress")
    public ResponseEntity<Double> getProgress(@PathVariable Long affectationId) {
        return ResponseEntity.ok(taskService.getInternshipProgress(affectationId));
    }
}