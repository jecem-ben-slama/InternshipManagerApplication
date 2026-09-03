package com.iit.internship_manager.services.interfaces;

import com.iit.internship_manager.domain.models.RendezVous;

public interface IMeetingReminderService {
    void send24HourReminder(RendezVous rendezVous);

    int processUpcoming24HourReminders();
}
