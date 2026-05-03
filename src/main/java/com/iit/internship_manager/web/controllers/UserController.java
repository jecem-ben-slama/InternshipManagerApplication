package com.iit.internship_manager.web.controllers;
import com.iit.internship_manager.services.interfaces.IUserService;
import com.iit.internship_manager.services.interfaces.IUserUpdateService;
import com.iit.internship_manager.web.dtos.ApiResponse;
import com.iit.internship_manager.web.dtos.UserResponseDTO;
import com.iit.internship_manager.web.dtos.updateUser.UpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final IUserService userService;
    private final IUserUpdateService userUpdateService;

    /**
     * GET /api/users/me
     * Fetches the profile of the currently authenticated user.
     * Essential for the Flutter app to load the local user state.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser() {
        UserResponseDTO profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success("Profil récupéré avec succès", profile));
    }

    /**
     * PUT /api/users/{id}
     * Updates user details using the Strategy Pattern.
     * Security: Logic inside service ensures users only update themselves unless
     * they are ADMIN_IT.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequest request) {

        UserResponseDTO updated = userUpdateService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur mis à jour avec succès", updated));
    }

    /**
     * GET /api/users/{id}
     * Allows administration and teachers to view specific student/colleague
     * profiles.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_IT', 'ENSEIGNANT')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getById(@PathVariable Long id) {
        UserResponseDTO user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur récupéré", user));
    }

    /**
     * GET /api/users/active
     * Admin dashboard view to manage all active users.
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN_IT')")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getAllActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<UserResponseDTO> users = userService.findAllActive(page, size);
        return ResponseEntity.ok(ApiResponse.success("Liste des utilisateurs actifs récupérée", users));
    }

    /**
     * DELETE /api/users/{id}
     * Performs a soft-delete (deactivation).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_IT')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur désactivé avec succès", null));
    }

    /**
     * POST /api/users/{id}/reactivate
     * Allows an Admin to restore a previously deactivated account.
     */
    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN_IT')")
    public ResponseEntity<ApiResponse<Void>> reactivate(@PathVariable Long id) {
        userService.reactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur réactivé avec succès", null));
    }
    // Suggested additions to UserController.java

    /**
     * GET /api/users/teachers
     * Fetches teachers within the user's department or filtered by department if
     * Admin.
     */
    @GetMapping("/teachers/available")
    public ResponseEntity<Page<UserResponseDTO>> getAvailableTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getTeachersByMyDepartment(page, size));
    }

    @GetMapping("/students/available")
    public ResponseEntity<Page<UserResponseDTO>> getAvailableStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getStudentsByMyDepartment(page, size));
    }
}