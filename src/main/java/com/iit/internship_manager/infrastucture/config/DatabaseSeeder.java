package com.iit.internship_manager.infrastucture.config;

import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.models.AcademicYear;
import com.iit.internship_manager.repositories.AcademicYearRepository;
import com.iit.internship_manager.services.registration.RegistrationStrategyFactory;
import com.iit.internship_manager.web.dtos.registration.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final RegistrationStrategyFactory factory;
    private final AcademicYearRepository academicYearRepository; // Added for Year Seeding

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Academic Years first (Crucial for system logic)
        seedAcademicYears();

        // 2. Seed users
        seedUser("admin@iit.tn", UserType.ADMIN_IT, DepartmentType.INFORMATIQUE);
        seedUser("teacher@iit.tn", UserType.TEACHER, DepartmentType.INFORMATIQUE);
        seedUser("student@iit.tn", UserType.STUDENT, DepartmentType.INFORMATIQUE);
    }

    private void seedAcademicYears() {
        if (academicYearRepository.count() == 0) {
            // Past Year 1
            academicYearRepository.save(new AcademicYear("2023-2024", false));
            // Past Year 2
            academicYearRepository.save(new AcademicYear("2024-2025", false));
            // Current Year (Active)
            academicYearRepository.save(new AcademicYear("2025-2026", true));

            System.out.println("✅ Successfully seeded Academic Years (Active: 2025-2026)");
        }
    }

    private void seedUser(String email, UserType type, DepartmentType dept) {
        try {
            RegisterRequest request = createRequest(email, type, dept);
            factory.resolve(type).register(request);
            System.out.println(" Successfully seeded " + type + " (" + dept + "): " + email);
        } catch (Exception e) {
            System.out.println(" Skipping " + type + " (" + email + "): " + e.getMessage());
        }
    }

    private RegisterRequest createRequest(String email, UserType type, DepartmentType dept) {
        RegisterRequest req;

        if (type == UserType.STUDENT) {
            StudentRegisterRequest studentReq = new StudentRegisterRequest();
            studentReq.setMatricule("2026-IIT-001");
            studentReq.setFiliere(Filiere.GENIE_LOGICIEL);
            studentReq.setAnneeEtude(AnneeEtude.ING2);
            req = studentReq;
        } else if (type == UserType.TEACHER) {
            TeacherRegisterRequest teacherReq = new TeacherRegisterRequest();
            teacherReq.setResponsablePFE(true);
            teacherReq.setQuotaAnnuel(5);
            teacherReq.setSpecialites(Set.of(SpecialiteType.DOT_NET));
            req = teacherReq;
        } else if (type == UserType.ADMIN_IT) {
            req = new AdminRegisterRequest();
        } else {
            throw new IllegalArgumentException("Unknown user type: " + type);
        }

        populateBaseFields(req, email, type, dept);
        return req;
    }

    private void populateBaseFields(RegisterRequest req, String email, UserType type, DepartmentType dept) {
        req.setEmail(email);
        req.setPassword("Password123");
        req.setNom("Test");
        req.setPrenom(type.name());
        req.setUserType(type);
        req.setDepartment(dept);
    }
}