package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.services.UserService;
import com.iit.internship_manager.services.updateUser.UserUpdateService;
import com.iit.internship_manager.web.dtos.ApiResponse;
import com.iit.internship_manager.web.dtos.UserResponseDTO;
import com.iit.internship_manager.web.dtos.updateUser.UpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserUpdateService userUpdateService;

    // * update user */
    @PutMapping("/{id}")
    public ApiResponse<UserResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequest request) {
        UserResponseDTO updated = userUpdateService.update(id, request);
        return ApiResponse.success("Utilisateur mis à jour avec succès", updated);
    }

    // * get user by ID */
    @GetMapping("/{id}")
    public ApiResponse<UserResponseDTO> getById(@PathVariable Long id) {
        return ApiResponse.success("Utilisateur récupéré", userService.findById(id));
    }

    // * get all active users */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN_IT')") // Only Admins can see the full user list
    public ApiResponse<List<UserResponseDTO>> getAllActive() {
        return ApiResponse.success("Liste des utilisateurs récupérée", userService.findAllActive());
    }

    // * deactivate user */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.success("Utilisateur désactivé avec succès", null);
    }

    // * reactivate user */
    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN_IT')")
    public ApiResponse<Void> reactivate(@PathVariable Long id) {
        userService.reactivate(id);
        return ApiResponse.success("Utilisateur réactivé avec succès", null);
    }
}