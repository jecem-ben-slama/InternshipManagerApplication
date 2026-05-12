package com.iit.internship_manager.web.dtos.updateUser;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.iit.internship_manager.domain.enums.DepartmentType;
import com.iit.internship_manager.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "Le role est obligatoire")
    private Role role;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prenom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotNull(message = "Le departement est obligatoire")
    private DepartmentType department;

    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caracteres")
    private String password;
}
