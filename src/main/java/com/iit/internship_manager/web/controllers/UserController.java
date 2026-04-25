package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.services.UserService;
import com.iit.internship_manager.services.updateUser.UserUpdateService;
import com.iit.internship_manager.web.dtos.UserResponseDTO;
import com.iit.internship_manager.web.dtos.updateUser.UpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserUpdateService userUpdateService;

    /**
     * Update an existing user.
     * The specific subclass (Student/Teacher) is handled automatically by the
     * UpdateRequest.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequest request) {
        return ResponseEntity.ok(userUpdateService.update(id, request));
    }

    /**
     * Get a single user's profile.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    /**
     * Get all active users (useful for Admin dashboards).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN_IT')") // Only Admins can see the full user list
    public ResponseEntity<List<UserResponseDTO>> getAllActive() {
        return ResponseEntity.ok(userService.findAllActive());
    }

    /**
     * Soft delete: Deactivates the user instead of deleting from DB.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivate a previously deactivated account.
     */
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivate(@PathVariable Long id) {
        userService.reactivate(id);
        return ResponseEntity.ok().build();
    }
}