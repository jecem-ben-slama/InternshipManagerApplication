package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.models.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    Page<File> findByAffectationIdOrderByUploadTimeDesc(Long affectationId, Pageable pageable);
}
