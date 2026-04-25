package com.iit.internship_manager.web.dtos.registration;

import com.iit.internship_manager.domain.enums.SpecialiteType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherRegisterRequest extends RegisterRequest {

    // false by default — the client sends true when registering
    // someone who is also responsable PFE at the time of creation.
    private boolean responsablePFE = false;

    @NotNull(message = "Le quota annuel est obligatoire")
    @Min(value = 0, message = "Le quota ne peut pas être négatif")
    private Integer quotaAnnuel;

    @NotEmpty(message = "Au moins une spécialité est requise")
    private Set<SpecialiteType> specialites = new HashSet<>();
}