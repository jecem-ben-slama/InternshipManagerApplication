package com.iit.internship_manager.infrastucture.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.reminders")
public class MeetingReminderProperties {
    private boolean enabled = true;
    private int windowHours = 24;
    private String cron = "0 0 * * * *";
}
