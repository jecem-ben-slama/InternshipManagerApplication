package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.AcademicYear;
import com.iit.internship_manager.repositories.AcademicYearRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentYearProvider {

    private final AcademicYearRepository academicYearRepository;

    /**
     * Returns the currently active academic year.
     * Throws an exception if no year is set to active in the database.
     */
    @Transactional(readOnly = true)
    public AcademicYear getCurrent() {
        return academicYearRepository.findByActiveTrue()
                .orElseThrow(
                        () -> new RuntimeException("Configuration Error: No active academic year found in database."));
    }
}