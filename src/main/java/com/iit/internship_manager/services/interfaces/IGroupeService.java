package com.iit.internship_manager.services.interfaces;

import com.iit.internship_manager.domain.models.Etudiant;
import com.iit.internship_manager.domain.models.Groupe;
import java.util.List;

public interface IGroupeService {
    /**
     * Finds an existing group with the exact same members or creates a new one.
     */
    Groupe getOrCreateGroup(Etudiant creator, List<Long> partnerIds);
}