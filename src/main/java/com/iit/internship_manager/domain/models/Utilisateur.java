package com.iit.internship_manager.domain.models;

import com.iit.internship_manager.domain.enums.Role;
import com.iit.internship_manager.domain.enums.DepartmentType; // Ensure this is imported
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "utilisateurs")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String nom;
    private String prenom;

    @Enumerated(EnumType.STRING)
    @Column(name = "department", nullable = false)
    private DepartmentType department; // Centralized field for all user types
}