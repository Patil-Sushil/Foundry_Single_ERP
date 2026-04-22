package com.kalibyte.foundry.common.email.impl;

import com.kalibyte.foundry.common.email.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@kalibyte.com}")
    private String fromEmail;

    @Value("${app.company.name:Kalibyte Foundry}")
    private String companyName;

    @Value("${app.company.address}")
    private String companyAddress;

    // ── SIMPLE TEXT EMAIL ──
    @Override
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    // ── HTML EMAIL ──
    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = isHtml
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email", e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    @Override
    public void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        context.setVariable("companyName", companyName);
        context.setVariable("companyAddress", companyAddress);
        String htmlBody = templateEngine.process("email/" + templateName, context);
        sendHtmlEmail(to, subject, htmlBody);
    }

    // ── EMAIL WITH ATTACHMENT ──
    @Override
    public void sendEmailWithAttachment(
            String to, String subject, String body,
            byte[] attachment, String fileName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.addAttachment(fileName, new ByteArrayResource(attachment));
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email with attachment", e);
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }

    @Override
    public void sendTemplatedEmailWithAttachment(
            String to, String subject, String templateName,
            Map<String, Object> variables, byte[] attachment, String fileName) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            context.setVariable("companyName", companyName);
            context.setVariable("companyAddress", companyAddress);
            String htmlBody = templateEngine.process("email/" + templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.addAttachment(fileName, new ByteArrayResource(attachment));
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send templated email with attachment", e);
            throw new RuntimeException("Failed to send templated email with attachment", e);
        }
    }
}