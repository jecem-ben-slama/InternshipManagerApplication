package com.iit.internship_manager.services.interfaces;

import com.iit.internship_manager.web.dtos.UserResponseDTO;
import com.iit.internship_manager.web.dtos.updateUser.UpdateRequest;

public interface IUserUpdateService {
    UserResponseDTO update(Long id, UpdateRequest dto);
}