package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.models.AcademicYear;
import com.iit.internship_manager.repositories.AcademicYearRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;

    @Transactional
    public void createYear(String yearId) {
        AcademicYear newYear = new AcademicYear(yearId, false);
        academicYearRepository.save(newYear);
    }

    /**
     * Switches the active year.
     * It deactivates the current one and activates the new one.
     */
    @Transactional
    public void activateYear(String yearId) {
        // 1. Deactivate all years
        List<AcademicYear> allYears = academicYearRepository.findAll();
        allYears.forEach(y -> y.setActive(false));
        academicYearRepository.saveAll(allYears);

        // 2. Activate the target year
        AcademicYear targetYear = academicYearRepository.findById(yearId)
                .orElseThrow(() -> new RuntimeException("Year not found: " + yearId));

        targetYear.setActive(true);
        academicYearRepository.save(targetYear);
    }
}