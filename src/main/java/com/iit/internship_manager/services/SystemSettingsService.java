package com.iit.internship_manager.services;

import org.springframework.stereotype.Service;

@Service
public class SystemSettingsService {
    // Default to the current academic cycle
    private String currentYear = "2025-2026";

    public String getCurrentYear() {
        return currentYear;
    }

    public void setCurrentYear(String newYear) {
        this.currentYear = newYear;
    }
}