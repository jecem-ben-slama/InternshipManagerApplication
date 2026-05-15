package com.iit.internship_manager.services.interfaces;

import com.iit.internship_manager.web.dtos.UserResponseDTO;
import com.iit.internship_manager.web.dtos.updateUser.UpdateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface IUserUpdateService {
    UserResponseDTO update(Long id, UpdateRequest dto);
    UserResponseDTO uploadCurrentUserProfilePhoto(MultipartFile file);
}
