package com.iit.internship_manager.services;

import com.iit.internship_manager.domain.enums.MeetingStatus;
import com.iit.internship_manager.domain.models.RendezVous;
import com.iit.internship_manager.infrastucture.config.MeetingReminderProperties;
import com.iit.internship_manager.repositories.RendezVousRepository;
import com.iit.internship_manager.services.interfaces.IEmailService;
import com.iit.internship_manager.services.interfaces.IMeetingReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingReminderServiceImpl implements IMeetingReminderService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IEmailService emailService;
    private final MeetingReminderProperties reminderProperties;
    private final RendezVousRepository rendezVousRepository;

    @Override
    public void send24HourReminder(RendezVous rendezVous) {
        if (!reminderProperties.isEnabled()) {
            log.info("24h meeting reminders are disabled. Skipping reminder for meeting {}", rendezVous.getId());
            return;
        }

        Context context = new Context();
        context.setVariable("objet", rendezVous.getObjet());
        context.setVariable("date", rendezVous.getDateHeure().format(FORMATTER));
        context.setVariable("lieu", rendezVous.getLieu());
        context.setVariable("teacherName",
                rendezVous.getAffectation().getEncadrant().getNom() + " " +
                        rendezVous.getAffectation().getEncadrant().getPrenom());
        context.setVariable("groupName", rendezVous.getAffectation().getGroupe().getNom());
        context.setVariable("windowHours", reminderProperties.getWindowHours());

        Set<String> recipients = new LinkedHashSet<>();
        recipients.add(rendezVous.getAffectation().getEncadrant().getEmail());
        rendezVous.getAffectation().getGroupe().getMembres().forEach(student -> recipients.add(student.getEmail()));

        String subject = "Rappel IIT StageManager: rendez-vous dans " + reminderProperties.getWindowHours() + "h";
        for (String recipient : recipients) {
            emailService.sendEmail(recipient, subject, "meeting-24h-reminder", context, null);
        }
    }

    @Override
    @Transactional
    @Scheduled(cron = "${app.reminders.cron}")
    public int processUpcoming24HourReminders() {
        if (!reminderProperties.isEnabled()) {
            log.debug("24h meeting reminder scheduler is disabled.");
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusHours(reminderProperties.getWindowHours());

        List<RendezVous> meetings = rendezVousRepository.findMeetingsPending24HourReminder(
                MeetingStatus.CONFIRMED,
                now,
                deadline
        );

        int processed = 0;
        for (RendezVous rendezVous : meetings) {
            try {
                send24HourReminder(rendezVous);
                rendezVous.setReminder24hSent(true);
                rendezVous.setReminder24hSentAt(LocalDateTime.now());
                processed += 1;
            } catch (Exception ex) {
                log.error("Failed to process 24h reminder for meeting {}", rendezVous.getId(), ex);
            }
        }

        if (processed > 0) {
            log.info("Processed {} meeting reminder(s) scheduled within the next {} hours.", processed, reminderProperties.getWindowHours());
        }

        return processed;
    }
}
