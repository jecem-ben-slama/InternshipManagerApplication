// RegisterRequest.java — abstract base with shared fields only
package com.iit.internship_manager.web.dtos.registration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.iit.internship_manager.domain.enums.UserType;
import jakarta.validation.constraints.*;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "userType", // the JSON field Jackson reads to pick the subclass
        visible = true // makes userType accessible on the object after deserialization
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AdminRegisterRequest.class, name = "ADMIN_IT"),
        @JsonSubTypes.Type(value = StudentRegisterRequest.class, name = "STUDENT"),
        @JsonSubTypes.Type(value = TeacherRegisterRequest.class, name = "TEACHER")
})
@Data
public abstract class RegisterRequest {

    @NotNull
    private UserType userType;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;
}