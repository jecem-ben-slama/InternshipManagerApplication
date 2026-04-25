package com.iit.internship_manager.web.dtos.updateUser;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Set;

import com.iit.internship_manager.domain.enums.SpecialiteType;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeacherUpdateDTO extends UpdateRequest {
    private boolean isResponsablePFE;
    private Set<SpecialiteType> specialites;
}