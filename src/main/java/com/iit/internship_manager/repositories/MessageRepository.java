package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.models.CandidatureMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<CandidatureMessage, Long> {
    // Fetches the chat history in order
    List<CandidatureMessage> findByCandidatureIdOrderBySentAtAsc(Long candidatureId);
}