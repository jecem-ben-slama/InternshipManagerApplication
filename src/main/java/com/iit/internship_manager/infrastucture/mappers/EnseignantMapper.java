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
    // MapStruct might complain if 'active' is not found via a standard setter.
    // Since it's in the parent Utilisateur, we ensure it's ignored during creation.
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "encadrementsActuels", ignore = true)
    // Mapping the new centralized department field inherited from RegisterRequest
    @Mapping(target = "department", source = "department")
    Enseignant toEntity(TeacherRegisterRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "encadrementsActuels", ignore = true)
    // Map the department from the UpdateRequest base class
    @Mapping(target = "department", source = "department")
    @Mapping(target = "responsablePFE", source = "responsablePFE")
    void updateEnseignantFromDto(TeacherUpdateDTO dto, @MappingTarget Enseignant entity);
}