package com.iit.internship_manager.services.interfaces;

import org.springframework.data.domain.Page;

import com.iit.internship_manager.domain.enums.DepartmentType;
import com.iit.internship_manager.web.dtos.UserResponseDTO;

public interface IUserService {
    UserResponseDTO getCurrentUserProfile();

    UserResponseDTO findById(Long id);

    Page<UserResponseDTO> findAllActive(int page, int size);

    void delete(Long id);

    void reactivate(Long id);

    boolean existsByEmail(String email);
    Page<UserResponseDTO> getTeachersByMyDepartment(int page, int size);
    Page<UserResponseDTO> getStudentsByMyDepartment(int page, int size);
    Page<UserResponseDTO> findAllByDepartment(DepartmentType department, int page, int size);
}