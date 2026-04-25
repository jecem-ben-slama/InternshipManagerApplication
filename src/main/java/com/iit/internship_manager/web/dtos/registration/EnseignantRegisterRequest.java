// EnseignantRegisterRequest.java
package com.iit.internship_manager.web.dtos.registration;

import com.iit.internship_manager.domain.enums.SpecialiteType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class EnseignantRegisterRequest extends RegisterRequest {

    // false by default — the client sends true when registering
    // someone who is also responsable PFE at the time of creation.
    private boolean responsablePFE= false;

    @NotEmpty
    private Set<SpecialiteType> specialites = new HashSet<>();
}