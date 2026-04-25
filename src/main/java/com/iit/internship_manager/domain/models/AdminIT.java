// AdminIT.java — added @DiscriminatorValue which was missing in your version
package com.iit.internship_manager.domain.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admins")
@DiscriminatorValue("ADMIN_IT")
@Getter
@Setter
public class AdminIT extends Utilisateur {
    // No extra fields. AdminIT is a plain Utilisateur.
}