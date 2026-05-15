package com.iit.internship_manager.web.controllers;

import com.iit.internship_manager.services.interfaces.IUserService;
import com.iit.internship_manager.services.interfaces.IUserUpdateService;
import com.iit.internship_manager.web.dtos.ApiResponse;
import com.iit.internship_manager.web.dtos.UserResponseDTO;
import com.iit.internship_manager.web.dtos.updateUser.UpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final IUserService userService;
    private final IUserUpdateService userUpdateService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser() {
        UserResponseDTO profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success("Profil recupere avec succes", profile));
    }

    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponseDTO>> uploadProfilePhoto(
            @RequestParam("file") MultipartFile file) {
        UserResponseDTO updated = userUpdateService.uploadCurrentUserProfilePhoto(file);
        return ResponseEntity.ok(ApiResponse.success("Photo de profil mise a jour avec succes", updated));
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<Resource> getProfilePhoto(@PathVariable Long id) {
        Resource resource = userService.loadProfilePhoto(id);
        String contentType = userService.getProfilePhotoContentType(id);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache().mustRevalidate().cachePrivate().sMaxAge(0, TimeUnit.SECONDS))
                .header(HttpHeaders.PRAGMA, "no-cache")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequest request) {

        UserResponseDTO updated = userUpdateService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur mis a jour avec succes", updated));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_IT', 'ENSEIGNANT')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getById(@PathVariable Long id) {
        UserResponseDTO user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur recupere", user));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN_IT')")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<UserResponseDTO> users = userService.findAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success("Liste complete des utilisateurs recuperee", users));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN_IT')")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getAllActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<UserResponseDTO> users = userService.findAllActive(page, size);
        return ResponseEntity.ok(ApiResponse.success("Liste des utilisateurs actifs recuperee", users));
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN_IT')")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getAllInactive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<UserResponseDTO> users = userService.findAllInactive(page, size);
        return ResponseEntity.ok(ApiResponse.success("Liste des utilisateurs desactives recuperee", users));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_IT')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur desactive avec succes", null));
    }

    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN_IT')")
    public ResponseEntity<ApiResponse<Void>> reactivate(@PathVariable Long id) {
        userService.reactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Utilisateur reactive avec succes", null));
    }

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
