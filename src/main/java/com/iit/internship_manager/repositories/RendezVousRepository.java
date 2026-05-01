package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.enums.MeetingStatus;
import com.iit.internship_manager.domain.models.RendezVous;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    // 1. Paginated history of all meetings for an internship
    Page<RendezVous> findByAffectationId(Long affectationId, Pageable pageable);

    // 2. Paginated upcoming meetings
    Page<RendezVous> findByAffectationIdAndDateHeureAfter(Long affectationId, LocalDateTime now, Pageable pageable);

    boolean existsByAffectationEncadrantEmailAndDateHeureBetweenAndIdNotAndStatusIn(
    String email, 
    LocalDateTime start, 
    LocalDateTime end, 
    Long id, 
    Collection<MeetingStatus> statuses
);
    // 3. Non-paginated upcoming meetings (often better for a "Next Meeting" widget
    // in Flutter)
    List<RendezVous> findTop5ByAffectationIdAndDateHeureAfterOrderByDateHeureAsc(Long affectationId, LocalDateTime now);
     
    @Query("SELECT COUNT(r) > 0 FROM RendezVous r " +
       "WHERE r.affectation.encadrant.email = :email " +
       "AND r.dateHeure < :end AND r.dateHeure > :start")
boolean existsByTeacherEmailAndDateBetween(
    @Param("email") String email, 
    @Param("start") LocalDateTime start, 
    @Param("end") LocalDateTime end
);

}