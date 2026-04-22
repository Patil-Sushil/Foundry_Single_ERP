package com.kalibyte.foundry.common.email;

import java.util.Map;

public interface EmailService {

    void sendEmail(String to, String subject, String body);

    void sendHtmlEmail(String to, String subject, String htmlBody);

    void sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> variables);

    void sendEmailWithAttachment(
            String to,
            String subject,
            String body,
            byte[] attachment,
            String fileName
    );

    void sendTemplatedEmailWithAttachment(
            String to,
            String subject,
            String templateName,
            Map<String, Object> variables,
            byte[] attachment,
            String fileName
    );
}