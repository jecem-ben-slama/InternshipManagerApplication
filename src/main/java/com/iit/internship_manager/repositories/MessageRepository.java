package com.iit.internship_manager.repositories;

import com.iit.internship_manager.domain.models.CandidatureMessage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<CandidatureMessage, Long> {
    // Fetches the chat history in order
    Page<CandidatureMessage> findByCandidatureIdOrderBySentAtDesc(Long candidatureId, Pageable pageable);  
      
    // MessageRepository.java
    Page<CandidatureMessage> findByCandidatureId(Long candidatureId, Pageable pageable);
}