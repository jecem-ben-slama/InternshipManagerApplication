package com.iit.internship_manager.web.dtos.updateUser;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUpdateDTO extends UpdateRequest {
    // Admins only use the base fields (nom, prenom, email)
}