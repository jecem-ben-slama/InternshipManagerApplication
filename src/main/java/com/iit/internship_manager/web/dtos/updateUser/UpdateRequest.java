package com.iit.internship_manager.web.dtos.updateUser;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.iit.internship_manager.domain.enums.DepartmentType; // New Import
import com.iit.internship_manager.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "userType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StudentUpdateDTO.class, name = "STUDENT"),
        @JsonSubTypes.Type(value = TeacherUpdateDTO.class, name = "TEACHER"),
        @JsonSubTypes.Type(value = AdminUpdateDTO.class, name = "ADMIN_IT")
})
public abstract class UpdateRequest {

    @NotNull(message = "L'ID est obligatoire")
    private Long id;

    private String userType;

    @NotNull(message = "Le rôle est obligatoire")
    private Role role;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotNull(message = "Le département est obligatoire")
    private DepartmentType department; // Centralized for updates
}