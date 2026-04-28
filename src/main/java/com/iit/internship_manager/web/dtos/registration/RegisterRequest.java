package com.iit.internship_manager.web.dtos.registration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.iit.internship_manager.domain.enums.DepartmentType; // New Import
import com.iit.internship_manager.domain.enums.UserType;
import jakarta.validation.constraints.*;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "userType", visible = true)
@JsonSubTypes({
                @JsonSubTypes.Type(value = AdminRegisterRequest.class, name = "ADMIN_IT"),
                @JsonSubTypes.Type(value = StudentRegisterRequest.class, name = "STUDENT"),
                @JsonSubTypes.Type(value = TeacherRegisterRequest.class, name = "TEACHER")
})
@Data
public abstract class RegisterRequest {

        @NotNull(message = "Le type d'utilisateur est obligatoire")
        private UserType userType;

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Format d'email invalide")
        private String email;

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
        private String password;

        @NotBlank(message = "Le nom est obligatoire")
        private String nom;

        @NotBlank(message = "Le prénom est obligatoire")
        private String prenom;

        @NotNull(message = "Le département est obligatoire")
        private DepartmentType department; // Centralized department field
}