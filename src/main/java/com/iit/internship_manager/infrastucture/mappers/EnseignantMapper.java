package com.iit.internship_manager.infrastucture.mappers;

import com.iit.internship_manager.domain.models.Enseignant;
import com.iit.internship_manager.web.dtos.registration.TeacherRegisterRequest;
import com.iit.internship_manager.web.dtos.updateUser.TeacherUpdateDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EnseignantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true) // Fixes the "Unmapped target property" error
    @Mapping(target = "encadrementsActuels", ignore = true)
    Enseignant toEntity(TeacherRegisterRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "encadrementsActuels", ignore = true)
    @Mapping(target = "responsablePFE", source = "responsablePFE")
    void updateEnseignantFromDto(TeacherUpdateDTO dto, @MappingTarget Enseignant entity);
}