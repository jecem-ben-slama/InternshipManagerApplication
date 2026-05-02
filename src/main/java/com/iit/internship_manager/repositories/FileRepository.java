package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.models.File;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    /**
     * Retrieves all file metadata associated with a specific internship
     * (Affectation).
     * This is used by students and teachers to view their project documents.
     */
    Page<File> findByAffectationId(Long affectationId, Pageable pageable);
    /**
     * Optional: Retrieves files uploaded by a specific user.
     * Useful if you want to show a "My Uploads" section.
     */
    List<File> findByOwnerId(Long ownerId);
}