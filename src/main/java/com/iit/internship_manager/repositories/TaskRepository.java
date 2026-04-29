package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.models.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // 1. Paginated tasks for a specific internship
    Page<Task> findByAffectationId(Long affectationId, Pageable pageable);

    // 2. Paginated tasks filtered by completion status
    Page<Task> findByAffectationIdAndCompleted(Long affectationId, boolean completed, Pageable pageable);

    // 3. Keep the count methods for progress bars (Counts don't need Pageable)
    long countByAffectationId(Long affectationId);

    long countByAffectationIdAndCompletedTrue(Long affectationId);
}