package com.iit.internship_manager.domain.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "admins")
@Getter
@Setter
public class AdminIT extends Utilisateur {
    // Add admin-specific fields here if needed in the future
}