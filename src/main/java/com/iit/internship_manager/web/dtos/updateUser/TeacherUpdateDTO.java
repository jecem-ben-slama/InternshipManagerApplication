package com.iit.internship_manager.web.dtos.updateUser;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Set;

import com.iit.internship_manager.domain.enums.SpecialiteType;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherUpdateDTO extends UpdateRequest {
    @JsonAlias("isResponsablePFE")
    private boolean responsablePFE;
    private Set<SpecialiteType> specialites;
    private Integer quotaAnnuel;
}
