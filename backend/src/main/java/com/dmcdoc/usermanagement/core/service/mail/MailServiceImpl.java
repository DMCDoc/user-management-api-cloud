package com.dmcdoc.usermanagement.core.service.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final String from;

    public MailServiceImpl(JavaMailSender mailSender,
            @Value("${spring.mail.from:no-reply@local}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Échec de l’envoi de l’email", e);
        }
    }
}
