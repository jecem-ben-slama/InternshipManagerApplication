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
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserUpdateService userUpdateService;

    /**
     * GET /api/users/me
     * Fetches the profile of the currently logged-in user.
     * Use this in Flutter as soon as the user logs in.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser() {
        return ResponseEntity.ok(ApiResponse.success(
                "Profil récupéré",
                userService.getCurrentUserProfile()));
    }

    // * update user */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequest request) {
        UserResponseDTO updated = userUpdateService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur mis à jour avec succès", updated));
    }

    // * get user by ID */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_IT') or hasRole('TEACHER')") // Only staff should browse all IDs
    public ResponseEntity<ApiResponse<UserResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Utilisateur récupéré", userService.findById(id)));
    }

    /**
     * GET /api/users/active?page=0&size=10
     * Switched to RequestParams for standard pagination
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN_IT')")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getAllActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(ApiResponse.success(
                "Liste des utilisateurs récupérée",
                userService.findAllActive(page, size)));
    }

    // * deactivate user */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_IT')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur désactivé avec succès", null));
    }

    // * reactivate user */
    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN_IT')")
    public ResponseEntity<ApiResponse<Void>> reactivate(@PathVariable Long id) {
        userService.reactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur réactivé avec succès", null));
    }
}