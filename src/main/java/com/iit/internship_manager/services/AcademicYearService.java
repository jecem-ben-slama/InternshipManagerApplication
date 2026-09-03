package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.enums.ErrorCode;
import com.iit.internship_manager.domain.exceptions.DomainException;
import com.iit.internship_manager.domain.models.AcademicYear;
import com.iit.internship_manager.repositories.AcademicYearRepository;
import com.iit.internship_manager.web.dtos.AcademicYearResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private static final Pattern YEAR_FORMAT = Pattern.compile("^\\d{4}-\\d{4}$");

    @Transactional(readOnly = true)
    public List<AcademicYearResponseDTO> getAllYears() {
        return academicYearRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(year -> new AcademicYearResponseDTO(year.getId(), year.isActive()))
                .toList();
    }

    @Transactional
    public void createYear(String yearId) {
        validateYearId(yearId);
        if (academicYearRepository.existsById(yearId)) {
            throw new DomainException(ErrorCode.CONFLICT, "Cette année universitaire existe déjà.");
        }

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
                .orElseThrow(() -> new DomainException(ErrorCode.RESOURCE_NOT_FOUND,
                        "Année universitaire introuvable : " + yearId));

        targetYear.setActive(true);
        academicYearRepository.save(targetYear);
    }

    private void validateYearId(String yearId) {
        if (yearId == null || !YEAR_FORMAT.matcher(yearId).matches()) {
            throw new DomainException(ErrorCode.VALIDATION_FAILED,
                    "Le format doit être AAAA-AAAA.");
        }

        String[] parts = yearId.split("-");
        int start = Integer.parseInt(parts[0]);
        int end = Integer.parseInt(parts[1]);
        if (end != start + 1) {
            throw new DomainException(ErrorCode.VALIDATION_FAILED,
                    "L'année universitaire doit être consécutive, par exemple 2025-2026.");
        }
    }
}
