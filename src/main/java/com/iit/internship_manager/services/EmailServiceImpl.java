package com.iit.internship_manager.services;

import com.iit.internship_manager.services.interfaces.IEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private static final String TEMPLATE_PREFIX = "emails/";
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void sendEmail(String to, String subject, String templateName, Context context, byte[] attachment) {
        log.info("[{}] Processing '{}' email for {}",
                Thread.currentThread().getName(), templateName, to);

        try {
            String html = templateEngine.process(TEMPLATE_PREFIX + templateName, context);
            MimeMessage message = mailSender.createMimeMessage();

            // Boolean 'true' indicates multipart message for attachments
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom("benslemajecem@gmail.com"); // Best practice to set the from address

            // --- ATTACHMENT FIX ---
            if (attachment != null && attachment.length > 0) {
                log.info("[{}] Attaching .ics file (Size: {} bytes)",
                        Thread.currentThread().getName(), attachment.length);

                // "text/calendar" ensures Gmail/Outlook recognizes it as a meeting invite
                helper.addAttachment("invite.ics",
                        new ByteArrayResource(attachment), "text/calendar");
            }

            mailSender.send(message);
            log.info("[{}] SUCCESS: Email delivered to {}", Thread.currentThread().getName(), to);

        } catch (MessagingException e) {
            log.error("[{}] SMTP ERROR: Failed to send to {}",
                    Thread.currentThread().getName(), to, e);
        }
    }
}