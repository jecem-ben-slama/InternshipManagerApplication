package com.iit.internship_manager.web.dtos;

public record AcceptanceResultDTO(
        Long affectationId,
        boolean responsableNotified,
        boolean studentsNotified,
        String notificationWarning) {
}