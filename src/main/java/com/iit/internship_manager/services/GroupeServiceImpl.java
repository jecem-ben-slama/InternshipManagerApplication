package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.exceptions.ResourceNotFoundException;
import com.iit.internship_manager.domain.exceptions.UnauthorizedActionException;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupeServiceImpl implements IGroupeService {

    private final GroupeRepository groupeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Groupe getOrCreateGroup(Etudiant creator, List<Long> partnerIds) {
        // 1. Initialize member list with the creator
        List<Etudiant> targetMembers = new ArrayList<>();
        targetMembers.add(creator);

        if (partnerIds != null && !partnerIds.isEmpty()) {
            // 2. Prevent self-addition (Creator cannot be their own partner)
            if (partnerIds.contains(creator.getId())) {
                throw new UnauthorizedActionException("Vous ne pouvez pas vous ajouter vous-même comme partenaire.");
            }

            // 3. Fetch partners and ensure they are actually Students (Type Safety)
            List<Etudiant> partners = userRepository.findAllById(partnerIds)
                    .stream()
                    .filter(u -> u instanceof Etudiant)
                    .map(u -> (Etudiant) u)
                    .collect(Collectors.toList());

            // 4. Validate that all requested IDs were found and are valid Students
            if (partners.size() < partnerIds.size()) {
                Long missingOrInvalidId = partnerIds.stream()
                        .filter(id -> partners.stream().noneMatch(p -> p.getId().equals(id)))
                        .findFirst()
                        .orElse(partnerIds.get(0));

                throw new ResourceNotFoundException("Etudiant Partenaire", missingOrInvalidId);
            }

            targetMembers.addAll(partners);
        }

        // 5. Check if a group with these exact members already exists
        // We look for groups containing the creator first to narrow the search
        List<Groupe> existingGroups = groupeRepository.findByMembresId(creator.getId());
        List<Long> targetIds = targetMembers.stream()
                .map(Etudiant::getId)
                .sorted()
                .toList();

        for (Groupe group : existingGroups) {
            List<Long> existingIds = group.getMembres().stream()
                    .map(Etudiant::getId)
                    .sorted()
                    .toList();

            if (existingIds.equals(targetIds)) {
                return group;
            }
        }

        // 6. Create new group if no match is found
        String groupName = generateGroupName(targetMembers);

        return groupeRepository.save(Groupe.builder()
                .nom(groupName)
                .membres(targetMembers)
                .build());
    }

    /**
     * Generates a descriptive name for the group based on membership.
     */
    private String generateGroupName(List<Etudiant> members) {
        if (members.size() == 1) {
            return "Solo: " + members.get(0).getNom() + " " + members.get(0).getPrenom();
        }
        return "Binôme: " + members.get(0).getNom() + " & " + members.get(1).getNom();
    }
}