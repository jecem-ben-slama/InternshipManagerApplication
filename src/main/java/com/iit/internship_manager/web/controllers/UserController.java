package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.services.UserService;
import com.iit.internship_manager.services.UserUpdateService;
import com.iit.internship_manager.web.dtos.ApiResponse;
import com.iit.internship_manager.web.dtos.UserResponseDTO;
import com.iit.internship_manager.web.dtos.updateUser.UpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page; // Add this import

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
    @GetMapping("/active/{page}")
    public ApiResponse<Page<UserResponseDTO>> getAllActive(@PathVariable int page) {
        // Adjusting for 0-based index if your frontend sends 1, 2, 3...
        // int adjustedPage = page > 0 ? page - 1 : 0;

        return ApiResponse.success(
                "Users retrieved successfully",
                userService.findAllActive(page));
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