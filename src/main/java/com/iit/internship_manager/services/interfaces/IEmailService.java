package com.iit.internship_manager.services.interfaces;

import org.thymeleaf.context.Context;

public interface IEmailService {
    void sendEmail(String to, String subject,
            String templateName, Context context,
            byte[] attachment);
}