package com.iit.internship_manager.services;

import com.iit.internship_manager.services.interfaces.IEmailService;
import jakarta.activation.DataHandler;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
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

    @Async("emailTaskExecutor") // Reference a specific executor if defined
    @Override
    public void sendEmail(String to, String subject, String templateName, Context context, byte[] attachment) {
        log.info("Processing '{}' email for {}", templateName, to);

        try {
            String htmlContent = templateEngine.process(TEMPLATE_PREFIX + templateName, context);
            MimeMessage message = mailSender.createMimeMessage();
            
            // 1. Set standard headers to help mail clients recognize calendar actions
            message.addHeader("Content-Class", "urn:content-classes:calendarmessage");
            message.addHeader("Description", subject);

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("benslemajecem@gmail.com");

            // 2. Create the Multipart container
            MimeMultipart multipart = new MimeMultipart("mixed");

            // 3. Add the HTML Body Part
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            // 4. Add the Calendar Part (The "Magic" Part)
            if (attachment != null && attachment.length > 0) {
                boolean isCancellation = templateName.contains("cancelled");
                String method = isCancellation ? "CANCEL" : "REQUEST"; 
                // Note: Using REQUEST instead of PUBLISH often triggers the "Accept/Decline" buttons in Gmail

                MimeBodyPart calPart = new MimeBodyPart();
                calPart.setHeader("Content-Type", "text/calendar; charset=UTF-8; method=" + method);
                calPart.setDataHandler(new DataHandler(new ByteArrayDataSource(attachment, "text/calendar")));
                multipart.addBodyPart(calPart);
                
                log.info("Calendar method set to: {}", method);
            }

            message.setContent(multipart);
            mailSender.send(message);
            
            log.info("SUCCESS: Actionable email sent to {}", to);

        } catch (Exception e) {
            log.error("SMTP ERROR: Failed to send actionable email to {}", to, e);
        }
    }
}