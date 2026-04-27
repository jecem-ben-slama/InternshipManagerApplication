package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.Etudiant;
import com.iit.internship_manager.domain.models.Groupe;
import com.iit.internship_manager.repositories.GroupeRepository;
import com.iit.internship_manager.repositories.UserRepository;
import com.iit.internship_manager.services.interfaces.IGroupeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupeServiceImpl implements IGroupeService {

    private final GroupeRepository groupeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Groupe getOrCreateGroup(Etudiant creator, List<Long> partnerIds) {
        // 1. Build the target member list (Creator + Partners)
        List<Etudiant> targetMembers = new ArrayList<>();
        targetMembers.add(creator);

        if (partnerIds != null && !partnerIds.isEmpty()) {
            List<Etudiant> partners = userRepository.findAllById(partnerIds)
                    .stream()
                    .map(u -> (Etudiant) u)
                    .toList();
            targetMembers.addAll(partners);
        }

        // 2. Search for an existing group with these exact members
        List<Groupe> existingGroups = groupeRepository.findByMembresId(creator.getId());

        for (Groupe group : existingGroups) {
            List<Long> existingIds = group.getMembres().stream().map(Etudiant::getId).toList();
            List<Long> targetIds = targetMembers.stream().map(Etudiant::getId).toList();

            if (existingIds.size() == targetIds.size() && existingIds.containsAll(targetIds)) {
                return group;
            }
        }

        // 3. Create new if not found
        String groupName = "Binôme: " + creator.getNom();
        if (targetMembers.size() > 1) {
            groupName += " & " + targetMembers.get(1).getNom();
        }

        return groupeRepository.save(Groupe.builder()
                .nom(groupName)
                .membres(targetMembers)
                .build());
    }
}