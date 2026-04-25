package com.iit.internship_manager.infrastucture.config;

import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.services.registration.RegistrationStrategyFactory;
import com.iit.internship_manager.web.dtos.registration.*; // Import all specific DTOs
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final RegistrationStrategyFactory factory;

    @Override
    public void run(String... args) throws Exception {
        seedUser("admin@iit.tn", UserType.ADMIN_IT);
        seedUser("teacher@iit.tn", UserType.TEACHER);
        seedUser("student@iit.tn", UserType.STUDENT);
    }

    private void seedUser(String email, UserType type) {
        try {
            RegisterRequest request = createRequest(email, type);
            factory.resolve(type).register(request);
            System.out.println(" Successfully seeded " + type + ": " + email);
        } catch (Exception e) {
            System.out.println("Skipping " + type + " (" + email + "): " + e.getMessage());
        }
    }

    private RegisterRequest createRequest(String email, UserType type) {
        if (type == UserType.STUDENT) {
            StudentRegisterRequest req = new StudentRegisterRequest();
            populateBaseFields(req, email, type);
            req.setMatricule("2026-IIT-001");
            req.setFiliere(Filiere.GENIE_LOGICIEL);
            req.setAnneeEtude(AnneeEtude.ING2);
            return req;
        }

        if (type == UserType.TEACHER) {
            TeacherRegisterRequest req = new TeacherRegisterRequest();
            populateBaseFields(req, email, type);
            req.setResponsablePFE(true);
            req.setQuotaAnnuel(5);
            req.setSpecialites(Set.of(SpecialiteType.JAVA_SPRING));         
            return req;
        }

        if (type == UserType.ADMIN_IT) {
            AdminRegisterRequest req = new AdminRegisterRequest();
            populateBaseFields(req, email, type);
            return req;
        }

        throw new IllegalArgumentException("Unknown user type: " + type);
    }

    // Helper to fill the common fields (email, password, etc.)
    private void populateBaseFields(RegisterRequest req, String email, UserType type) {
        req.setEmail(email);
        req.setPassword("Password123");
        req.setNom("Test");
        req.setPrenom(type.name());
        req.setUserType(type);
    }
}