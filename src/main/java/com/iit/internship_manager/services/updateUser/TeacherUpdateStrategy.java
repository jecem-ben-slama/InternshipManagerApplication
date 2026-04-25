package com.iit.internship_manager.services.updateUser;

import com.iit.internship_manager.domain.models.Enseignant;
import com.iit.internship_manager.domain.models.Utilisateur;
import com.iit.internship_manager.infrastucture.mappers.EnseignantMapper;
import com.iit.internship_manager.web.dtos.updateUser.TeacherUpdateDTO;
import com.iit.internship_manager.web.dtos.updateUser.UpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeacherUpdateStrategy implements UserUpdateStrategy {

    private final EnseignantMapper enseignantMapper;

    @Override
    public boolean supports(Utilisateur user, UpdateRequest dto) {
        return user instanceof Enseignant && "TEACHER".equals(dto.getUserType());
    }

    @Override
    public void update(Utilisateur user, UpdateRequest dto) {
        Enseignant enseignant = (Enseignant) user;
        TeacherUpdateDTO tDto = (TeacherUpdateDTO) dto;

        // 1. Map general fields (nom, prenom, specialites, responsablePFE)
        enseignantMapper.updateEnseignantFromDto(tDto, enseignant);

        // 2. Enforce Business Rule: New Quota >= Current Workload
        if (tDto.getQuotaAnnuel() != null) {
            int currentLoad = enseignant.getEncadrementsActuels();
            int newQuota = tDto.getQuotaAnnuel();

            if (newQuota < currentLoad) {
                throw new IllegalArgumentException(
                        String.format("Impossible de réduire le quota à %d car l'enseignant encadre déjà %d étudiants.",
                                newQuota, currentLoad));
            }

            enseignant.setQuotaAnnuel(newQuota);
        }
    }
}