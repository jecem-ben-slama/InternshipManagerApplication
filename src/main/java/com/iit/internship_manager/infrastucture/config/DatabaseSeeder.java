package com.iit.internship_manager.infrastucture.config;

import com.iit.internship_manager.domain.enums.*;
import com.iit.internship_manager.domain.models.Affectation;
import com.iit.internship_manager.domain.models.AcademicYear;
import com.iit.internship_manager.domain.models.Candidature;
import com.iit.internship_manager.domain.models.Enseignant;
import com.iit.internship_manager.domain.models.Etudiant;
import com.iit.internship_manager.domain.models.Groupe;
import com.iit.internship_manager.domain.models.Sujet;
import com.iit.internship_manager.repositories.AcademicYearRepository;
import com.iit.internship_manager.repositories.AffectationRepository;
import com.iit.internship_manager.repositories.CandidatureRepository;
import com.iit.internship_manager.repositories.GroupeRepository;
import com.iit.internship_manager.repositories.SubjectRepository;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.services.registration.RegistrationStrategyFactory;
import com.iit.internship_manager.web.dtos.registration.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final RegistrationStrategyFactory factory;
    private final AcademicYearRepository academicYearRepository; // Added for Year Seeding
    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final GroupeRepository groupeRepository;
    private final CandidatureRepository candidatureRepository;
    private final AffectationRepository affectationRepository;

    @Override
    public void run(String... args) throws Exception {
        repairUserInheritanceData();

        // 1. Seed Academic Years first (Crucial for system logic)
        seedAcademicYears();

        // 2. Seed users
        seedUser("admin@iit.tn", UserType.ADMIN_IT, DepartmentType.INFORMATIQUE);
        seedUser("teacher@iit.tn", UserType.TEACHER, DepartmentType.INFORMATIQUE);
        seedUser("student@iit.tn", UserType.STUDENT, DepartmentType.INFORMATIQUE);
        seedDemoData();
    }

    private void repairUserInheritanceData() {
        jdbcTemplate.update("UPDATE utilisateurs SET role = 'ENSEIGNANT' WHERE user_type = 'TEACHER' AND role IS NULL");
        jdbcTemplate.update("UPDATE utilisateurs SET role = 'ETUDIANT' WHERE user_type = 'STUDENT' AND role IS NULL");
        jdbcTemplate.update("UPDATE utilisateurs SET role = 'ADMIN_IT' WHERE user_type = 'ADMIN_IT' AND role IS NULL");

        jdbcTemplate.update("""
                INSERT INTO enseignants (id, encadrements_actuels, quota_annuel, responsable_pfe)
                SELECT u.id, 0, 5, false
                FROM utilisateurs u
                LEFT JOIN enseignants e ON e.id = u.id
                WHERE u.user_type = 'TEACHER' AND e.id IS NULL
                """);

        jdbcTemplate.update("""
                INSERT INTO etudiants (id, annee_etude, filiere, matricule)
                SELECT u.id, 'ING2', 'GENIE_LOGICIEL', CONCAT('AUTO-', u.id)
                FROM utilisateurs u
                LEFT JOIN etudiants e ON e.id = u.id
                WHERE u.user_type = 'STUDENT' AND e.id IS NULL
                """);

        jdbcTemplate.update("""
                INSERT INTO admins (id)
                SELECT u.id
                FROM utilisateurs u
                LEFT JOIN admins a ON a.id = u.id
                WHERE u.user_type = 'ADMIN_IT' AND a.id IS NULL
                """);

        jdbcTemplate.update("UPDATE enseignants SET encadrements_actuels = 0 WHERE encadrements_actuels IS NULL");
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

    private void seedDemoData() {
        AcademicYear currentYear = academicYearRepository.findByActiveTrue().orElse(null);
        if (currentYear == null) {
            return;
        }

        Enseignant teacher = userRepository.findByEmail("teacher@iit.tn")
                .filter(Enseignant.class::isInstance)
                .map(Enseignant.class::cast)
                .orElse(null);
        Etudiant student = userRepository.findByEmail("student@iit.tn")
                .filter(Etudiant.class::isInstance)
                .map(Etudiant.class::cast)
                .orElse(null);

        if (teacher == null || student == null) {
            return;
        }

        Sujet availableSubject = seedAvailableTeacherSubject(teacher, currentYear);
        Sujet studentProposal = seedStudentProposal(teacher, student, currentYear);
        Groupe groupe = seedGroup(student);
        Candidature candidature = seedAcceptedCandidature(groupe, availableSubject, currentYear);
        seedAffectation(groupe, teacher, availableSubject, candidature, currentYear);
    }

    private Sujet seedAvailableTeacherSubject(Enseignant teacher, AcademicYear currentYear) {
        return subjectRepository.findAll().stream()
                .filter(subject -> "Plateforme de suivi des stages".equals(subject.getTitre()))
                .findFirst()
                .orElseGet(() -> {
                    Sujet sujet = new Sujet();
                    sujet.setTitre("Plateforme de suivi des stages");
                    sujet.setDescription("Sujet de démonstration visible dans le frontend pour valider l'intégration admin.");
                    sujet.setStatut(SujetStatus.AVAILABLE);
                    sujet.setType(SujetType.PFE);
                    sujet.setEnseignant(teacher);
                    sujet.setAnneeUniversitaire(currentYear);
                    sujet.setTechnologies(List.of("Angular", "Spring Boot", "MySQL"));
                    return subjectRepository.save(sujet);
                });
    }

    private Sujet seedStudentProposal(Enseignant teacher, Etudiant student, AcademicYear currentYear) {
        return subjectRepository.findAll().stream()
                .filter(subject -> "Application mobile de gestion PFA".equals(subject.getTitre()))
                .findFirst()
                .orElseGet(() -> {
                    Sujet sujet = new Sujet();
                    sujet.setTitre("Application mobile de gestion PFA");
                    sujet.setDescription("Sujet proposé par l'étudiant pour nourrir les listes et détails côté frontend.");
                    sujet.setStatut(SujetStatus.PROPOSED_BY_STUDENT);
                    sujet.setType(SujetType.PFA);
                    sujet.setEnseignant(teacher);
                    sujet.setProposant(student);
                    sujet.setAnneeUniversitaire(currentYear);
                    sujet.setTechnologies(List.of("Flutter", "REST API", "Firebase"));
                    return subjectRepository.save(sujet);
                });
    }

    private Groupe seedGroup(Etudiant student) {
        return groupeRepository.findAll().stream()
                .filter(group -> "Groupe Demo IIT".equals(group.getNom()))
                .findFirst()
                .orElseGet(() -> {
                    Groupe groupe = Groupe.builder().nom("Groupe Demo IIT").build();
                    groupe.addMembre(student);
                    return groupeRepository.save(groupe);
                });
    }

    private Candidature seedAcceptedCandidature(Groupe groupe, Sujet sujet, AcademicYear currentYear) {
        return candidatureRepository.findAll().stream()
                .filter(item -> item.getGroupe() != null
                        && item.getSujet() != null
                        && item.getGroupe().getId().equals(groupe.getId())
                        && item.getSujet().getId().equals(sujet.getId()))
                .findFirst()
                .orElseGet(() -> {
                    Candidature candidature = new Candidature();
                    candidature.setGroupe(groupe);
                    candidature.setSujet(sujet);
                    candidature.setAnneeUniversitaire(currentYear);
                    candidature.setStatut(DemandeStatus.ACCEPTED_BY_TEACHER);
                    return candidatureRepository.save(candidature);
                });
    }

    private void seedAffectation(Groupe groupe, Enseignant teacher, Sujet sujet, Candidature candidature,
            AcademicYear currentYear) {
        boolean alreadyExists = affectationRepository.findAll().stream()
                .anyMatch(affectation -> affectation.getSujet() != null
                        && affectation.getSujet().getId().equals(sujet.getId()));
        if (alreadyExists) {
            return;
        }

        Affectation affectation = Affectation.builder()
                .groupe(groupe)
                .encadrant(teacher)
                .sujet(sujet)
                .originalCandidature(candidature)
                .status("IN_PROGRESS")
                .dateAffectation(LocalDateTime.now().minusDays(2))
                .anneeUniversitaire(currentYear)
                .build();
        affectationRepository.save(affectation);

        sujet.setStatut(SujetStatus.TAKEN);
        subjectRepository.save(sujet);

        teacher.setEncadrementsActuels(Math.max(teacher.getEncadrementsActuels(), 1));
        userRepository.save(teacher);
    }
}
