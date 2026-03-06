package com.kalibyte.foundry.common.email;

public interface EmailService {

    void sendEmail(String to, String subject, String body);

    void sendEmailWithAttachment(
            String to,
            String subject,
            String body,
            byte[] attachment,
            String fileName
    );
}