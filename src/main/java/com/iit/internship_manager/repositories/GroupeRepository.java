package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.models.Groupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupeRepository extends JpaRepository<Groupe, Long> {

    /**
     * Finds all groups that a specific student is a member of.
     * Useful for showing the student's history or current applications.
     */
    @Query("SELECT g FROM Groupe g JOIN g.membres m WHERE m.id = :studentId")
    List<Groupe> findByStudentId(@Param("studentId") Long studentId);
    
    List<Groupe> findByMembresId(Long studentId);
}