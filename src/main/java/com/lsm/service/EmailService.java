package com.lsm.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.SendEmailRequest;
import com.resend.services.emails.model.SendEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
public class EmailService {
    private final TemplateEngine templateEngine;
    private final Resend resend;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${RESEND_FROM_EMAIL:noreply@learnovify.com}")
    private String fromEmail;

    public EmailService(TemplateEngine templateEngine, @Value("${RESEND_API_KEY}") String resendApiKey) {
        this.templateEngine = templateEngine;
        this.resend = new Resend(resendApiKey);
    }

    @Async
    public void sendPasswordResetEmail(String to, String resetToken, String name) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("resetLink", frontendUrl + "/reset-password?token=" + resetToken);
            context.setVariable("expirationHours", "1");

            String emailContent = templateEngine.process("password-reset-email", context);
            sendEmail(to, "Password Reset Request", emailContent);

            log.info("Password reset email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Async
    public void sendPasswordResetConfirmationEmail(String to, String name) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("loginLink", frontendUrl + "/login");

            String emailContent = templateEngine.process("password-reset-confirmation-email", context);
            sendEmail(to, "Password Reset Successful", emailContent);

            log.info("Password reset confirmation email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset confirmation email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset confirmation email", e);
        }
    }

    private void sendEmail(String to, String subject, String content) throws ResendException {
        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .from(fromEmail)
                .to(to)
                .subject(subject)
                .html(content)
                .build();

        SendEmailResponse response = resend.emails().send(emailRequest);
        log.debug("Email sent with ID: {}", response.getId());
    }
}