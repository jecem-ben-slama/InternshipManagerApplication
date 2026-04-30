// RegistrationStrategyFactory.java
package com.iit.internship_manager.services.registration;

import com.iit.internship_manager.domain.enums.ErrorCode;
import com.iit.internship_manager.domain.enums.UserType;
import com.iit.internship_manager.domain.exceptions.DomainException;
import com.iit.internship_manager.web.dtos.registration.RegisterRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RegistrationStrategyFactory {

    // Spring automatically collects every @Service bean that implements
    // RegistrationStrategy and injects them here as a list.
    // No manual wiring needed — adding a new strategy is enough.
    private final List<RegistrationStrategy<?>> strategies;

    @SuppressWarnings("unchecked")
    public <T extends RegisterRequest> RegistrationStrategy<T> resolve(UserType type) {
        return (RegistrationStrategy<T>) strategies.stream()
                .filter(s -> s.getSupportedType() == type)
                .findFirst()
                .orElseThrow(() -> new DomainException(ErrorCode.FORBIDDEN, "Combinaison type/utilisateur non supportée"));
    }
}